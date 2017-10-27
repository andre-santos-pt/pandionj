package pt.iscte.pandionj.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.ExceptionType;
import pt.iscte.pandionj.FigureProvider;
import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.Utils;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel.StackEvent;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.ModelObserver;

public class StackFrameFigure extends Figure {
	final IStackFrameModel frame;
	private GridLayout layout;
	private FigureProvider figProvider;
	private ObjectContainer objectContainer;
	private RuntimeViewer runtimeViewer;
	private boolean invisible;
	private boolean instance;
	private Label label;
	private List<IReferenceModel> pointers;
	
	public StackFrameFigure(RuntimeViewer runtimeViewer, IStackFrameModel frame, ObjectContainer objectContainer, boolean invisible, boolean instance) {
		this.runtimeViewer = runtimeViewer;
		this.frame = frame;
		this.figProvider = runtimeViewer.getFigureProvider();
		this.objectContainer = objectContainer;
		this.invisible = invisible;
		this.instance = instance;
		this.pointers = new ArrayList<IReferenceModel>();
		setBackgroundColor(Constants.Colors.VIEW_BACKGROUND);
		layout = new GridLayout(1, false);
		layout.verticalSpacing = 4;
		layout.horizontalSpacing = 2;
		setLayoutManager(layout);
		if(!invisible) {
			setOpaque(true);
			setBorder(new LineBorder(ColorConstants.gray, 2));
			label = new Label(frame.getInvocationExpression());
			label.setForegroundColor(ColorConstants.gray);
			add(label);
			label.addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent me) {}
				public void mouseReleased(MouseEvent me) {}
				public void mouseDoubleClicked(MouseEvent me) {
					PandionJUI.navigateToLine(frame.getSourceFile(), frame.getLineNumber()-1);
				}
			});
		}
		
		ExceptionType exception = ExceptionType.match(frame.getExceptionType());
		for (IVariableModel<?> v : frame.getStackVariables()) {
			addVariable(v, exception);
		}

		updateLook(frame, false);
		addFrameObserver(frame);
	}

	private void addFrameObserver(IStackFrameModel frame) {
		frame.registerObserver(new ModelObserver<StackEvent<?>>() {
			@Override
			public void update(StackEvent<?> event) {
				if(event != null) {
					ExceptionType exception = null;
					if(event.type == StackEvent.Type.NEW_VARIABLE) {
						addVariable((IVariableModel<?>) event.arg, exception);
					}
					else if(event.type == StackEvent.Type.VARIABLE_OUT_OF_SCOPE) {
						PandionJFigure<?> toRemove = getVariableFigure((IVariableModel<?>)  event.arg); 
						if(toRemove != null)
							remove(toRemove);

						if(event.arg instanceof IReferenceModel) {
							runtimeViewer.removePointer((IReferenceModel) event.arg);
							pointers.remove((IReferenceModel) event.arg);
						}
					}
					else if(event.type == StackEvent.Type.EXCEPTION) {
						exception = ExceptionType.match((String) event.arg);
					}

					for (IVariableModel<?> v : frame.getStackVariables()) {
						if(v instanceof IReferenceModel) {
							IReferenceModel ref = (IReferenceModel) v;
							objectContainer.updateIllustration(ref, exception);
						}
					}
				}
				updateLook(frame, false);
			}
		});
	}

	private void updateLook(IStackFrameModel model, boolean termination) {
		if(!invisible) {
			if(model.isObsolete() || termination) {
				setBackgroundColor(Constants.Colors.OBSOLETE);
				setBorder(new LineBorder(model.exceptionOccurred() ?  Constants.Colors.ERROR : ColorConstants.lightGray, 2, SWT.LINE_DASH));
			}
			else if(model.exceptionOccurred()) {
				setBackgroundColor(Constants.Colors.INST_POINTER);
				setBorder(new LineBorder(Constants.Colors.ERROR, Constants.STACKFRAME_LINE_WIDTH, SWT.LINE_DASH));
				paintNullRefs();
			}
			else if(model.isExecutionFrame())
				setBackgroundColor(Constants.Colors.INST_POINTER);
			else
				setBackgroundColor(Constants.Colors.VIEW_BACKGROUND);
		}
		layout.layout(this);
		if(label != null && frame.getSourceFile() != null && frame.getLineNumber() != -1) 
			label.setToolTip(new Label(frame.getSourceFile().getName() + " (line " + frame.getLineNumber() +")"));
	}

	private void paintNullRefs() {
		for (Object object : getChildren()) {
			if(object instanceof ReferenceFigure) {
				ReferenceFigure ref = (ReferenceFigure) object;
				if(ref.model.getModelTarget().isNull())
					ref.setError();
			}
		}
	}
	private void addVariable(IVariableModel<?> v, ExceptionType exception) {
		if(v.isInstance() == instance) { // && !(v instanceof IReferenceModel && !((IReferenceModel) v).getTags().isEmpty())) {
			PandionJFigure<?> figure = figProvider.getFigure(v, true);
			add(figure);

			layout.setConstraint(figure, new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));

			if(v instanceof IReferenceModel) {
				IReferenceModel ref = (IReferenceModel) v;
				IEntityModel target = ref.getModelTarget();
				PandionJFigure<?> targetFig = null;
				if(!target.isNull())
					targetFig = objectContainer.addObject(target);
				addPointer((ReferenceFigure) figure, ref, target, targetFig);
				objectContainer.updateIllustration(ref, exception);
			}
		}
	}

	private void addPointer(ReferenceFigure figure, IReferenceModel ref, IEntityModel target, PandionJFigure<?> targetFig) {
		PolylineConnection pointer = new PolylineConnection();
		pointer.setVisible(!target.isNull());
		pointer.setSourceAnchor(figure.getAnchor());
		if(target.isNull())
			pointer.setSourceAnchor(figure.getAnchor());
		else
			pointer.setTargetAnchor(targetFig.getIncommingAnchor());
		Utils.addArrowDecoration(pointer);
		addPointerObserver(ref, pointer);
		runtimeViewer.addPointer(ref, pointer);
		pointers.add(ref);
	}

	private void addPointerObserver(IReferenceModel ref, PolylineConnection pointer) {
		ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
			@Override
			public void update(IEntityModel arg) {
				IEntityModel target = ref.getModelTarget();
				pointer.setVisible(!target.isNull());
				if(!target.isNull()) {
					PandionJFigure<?> targetFig = objectContainer.addObject(target);
					pointer.setTargetAnchor(targetFig.getIncommingAnchor());
					Utils.addArrowDecoration(pointer);
				}
			}
		});
	}


	public PandionJFigure<?> getVariableFigure(IVariableModel<?> v) {
		for (Object object : getChildren()) {
			if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == v)
				return (PandionJFigure<?>) object;
		}
		return null;
	}
	
	
	public void removePointers() {
		for (IReferenceModel r : pointers)
			runtimeViewer.removePointer(r);
		pointers.clear();
	}
}
