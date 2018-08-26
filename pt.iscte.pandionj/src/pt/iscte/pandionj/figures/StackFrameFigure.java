package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.FigureProvider;
import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.extensibility.ExceptionType;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel.StackEvent;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.ModelObserver;
import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class StackFrameFigure extends Figure {
	final IStackFrameModel frame;
	private GridLayout layout;
	private FigureProvider figProvider;
	private ObjectContainer objectContainer;
	private RuntimeViewer runtimeViewer;
	private boolean invisible;
	private boolean instance;
	private Label label;

	public StackFrameFigure(RuntimeViewer runtimeViewer, IStackFrameModel frame, ObjectContainer objectContainer, boolean invisible, boolean instance) {
		this.runtimeViewer = runtimeViewer;
		this.frame = frame;
		this.figProvider = runtimeViewer.getFigureProvider();
		this.objectContainer = objectContainer;
		this.invisible = invisible;
		this.instance = instance;

		setBackgroundColor(PandionJConstants.Colors.VIEW_BACKGROUND);
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

//		ExceptionType exception = ExceptionType.match(frame.getExceptionType());
		for (IVariableModel<?> v : frame.getAllVariables()) {
			addVariable(v);
		}

		if(frame.exceptionOccurred()) {
			Object illustrationArg = null;
			StackEvent<String> exception = frame.getExceptionEvent();
			if(exception.type == StackEvent.Type.ARRAY_INDEX_EXCEPTION) {
				illustrationArg = new Integer(exception.arg);
			}
			else if(exception.type == StackEvent.Type.EXCEPTION) {
				// TODO exception handling
//				illustrationArg = ExceptionType.match((String) exception.arg);
			}
			for (IReferenceModel ref : frame.getReferenceVariables())
				objectContainer.updateIllustration(ref, illustrationArg);
		}
			
		updateLook(frame, false);
		addFrameObserver(frame);
	}

	private void addFrameObserver(IStackFrameModel frame) {
		frame.registerDisplayObserver(new ModelObserver<StackEvent<?>>() {
			@Override
			public void update(StackEvent<?> event) {
				if(event != null) {
					Object illustrationArg = null;
					if(event.type == StackEvent.Type.NEW_VARIABLE) {
						addVariable((IVariableModel<?>) event.arg);
					}
					else if(event.type == StackEvent.Type.VARIABLE_OUT_OF_SCOPE) {
						PandionJFigure<?> toRemove = getVariableFigure((IVariableModel<?>)  event.arg); 
						if(toRemove != null)
							remove(toRemove);

						if(event.arg instanceof IReferenceModel) {
							runtimeViewer.removePointer((IReferenceModel) event.arg);
						}
					}
					else if(event.type == StackEvent.Type.ARRAY_INDEX_EXCEPTION) {
						illustrationArg = new Integer((String) event.arg);
//						illustrationArg = ExceptionType.match((String) event.arg);
					}
					else if(event.type == StackEvent.Type.EXCEPTION) {
						illustrationArg = ExceptionType.match((String) event.arg);
					}
					else if (event.type == StackEvent.Type.RETURN_VALUE) {
						label.setText(label.getText() + " = " + event.arg);
					}
					
					for (IReferenceModel ref : frame.getReferenceVariables())
						objectContainer.updateIllustration(ref, illustrationArg);
				}
				updateLook(frame, false);
			}
		});
	}

	private void updateLook(IStackFrameModel model, boolean termination) {
		if(!invisible) {
			if(model.isObsolete() || termination) {
				setBackgroundColor(PandionJConstants.Colors.OBSOLETE);
				setBorder(new LineBorder(model.exceptionOccurred() ?  PandionJConstants.Colors.ERROR : ColorConstants.lightGray, 2, SWT.LINE_DASH));
			}
			else if(model.exceptionOccurred()) {
				setBackgroundColor(PandionJConstants.Colors.INST_POINTER);
				setBorder(new LineBorder(PandionJConstants.Colors.ERROR, PandionJConstants.STACKFRAME_LINE_WIDTH, SWT.LINE_DASH));

				if(model.getExceptionType().equals(NullPointerException.class.getName()))
					paintNullRefs();

				Label labelExc = new Label(PandionJConstants.Messages.prettyException(model.getExceptionType()));
				labelExc.setForegroundColor(PandionJConstants.Colors.ERROR);
				setToolTip(labelExc);
			}
			else if(model.isExecutionFrame())
				setBackgroundColor(PandionJConstants.Colors.INST_POINTER);
			else
				setBackgroundColor(PandionJConstants.Colors.VIEW_BACKGROUND);
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

	private void addVariable(IVariableModel<?> v) {
		if(v.isInstance() == instance) { // && !(v instanceof IReferenceModel && !((IReferenceModel) v).getTags().isEmpty())) {
			PandionJFigure<?> figure = figProvider.getFigure(v, true);
			add(figure);
			layout.setConstraint(figure, new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));

			if(v instanceof IReferenceModel)
				objectContainer.addObjectAndPointer((IReferenceModel) v, ((ReferenceFigure) figure).getAnchor());
		}
	}

	public PandionJFigure<?> getVariableFigure(IVariableModel<?> v) {
		for (Object object : getChildren()) {
			if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == v)
				return (PandionJFigure<?>) object;
		}
		return null;
	}
}
