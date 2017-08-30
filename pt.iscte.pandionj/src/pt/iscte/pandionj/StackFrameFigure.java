package pt.iscte.pandionj;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolylineConnection;

import pt.iscte.pandionj.RuntimeViewer.ObjectContainer;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel.StackEvent;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.PositionAnchor;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.model.ModelObserver;

public class StackFrameFigure extends Figure {
	private GridLayout layout;
	private FigureProvider figProvider;
	private ObjectContainer objectContainer;
	private Figure rootPane;
	private Map<IReferenceModel, PolylineConnection> pointerMap;
	private boolean invisible;
	
	public StackFrameFigure(Figure rootPane, IStackFrameModel frame, ObjectContainer objectContainer, boolean invisible) {
		this.rootPane = rootPane;
		this.objectContainer = objectContainer;
		this.invisible = invisible;

		setBackgroundColor(Constants.Colors.OBJECT);
		layout = new GridLayout(1, false);
		layout.verticalSpacing = 2;
		setLayoutManager(layout);
		if(!invisible) {
			setOpaque(true);
			setBorder(new LineBorder(ColorConstants.lightGray, 1));
			Label label = new Label(frame.getInvocationExpression());
			label.setForegroundColor(Constants.Colors.CONSTANT);
			add(label);
			layout.setConstraint(label, Constants.RIGHT_ALIGN);
		}
		pointerMap = new HashMap<>();
		figProvider = new FigureProvider(frame);
		for (IVariableModel<?> v : frame.getStackVariables())
			add(v);
		addFrameObserver(frame);
		layout.layout(this);
		updateLook(frame);
	}

	public void clearPointers() {
		for (PolylineConnection conn : pointerMap.values())
			rootPane.remove(conn);
	}

	private void addFrameObserver(IStackFrameModel frame) {
		frame.registerDisplayObserver(new ModelObserver<StackEvent<?>>() {
			@Override
			public void update(StackEvent<?> event) {
				if(event != null) {
					ExceptionType exception = null;
					if(event.type == StackEvent.Type.NEW_VARIABLE) {
						add((IVariableModel<?>) event.arg);
					}
					else if(event.type == StackEvent.Type.VARIABLE_OUT_OF_SCOPE) {
						PandionJFigure<?> toRemove = getVariableFigure((IVariableModel<?>)  event.arg); 
						if(toRemove != null)
							remove(toRemove);

						PolylineConnection conn = pointerMap.get(event.arg);
						if(conn != null)
							rootPane.remove(conn);
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
				layout.layout(StackFrameFigure.this);
				updateLook(frame);
			}
		});
	}

	private void updateLook(IStackFrameModel model) {
		if(!invisible) {
			if(model.isObsolete())
				setBorder(new LineBorder(Constants.Colors.OBSOLETE, 2));
			//					StackFrameViewer.this.setBackgroundColor(Constants.Colors.OBSOLETE);
			else if(model.exceptionOccurred())
				setBorder(new LineBorder(Constants.Colors.ERROR, 2));
			else if(model.isExecutionFrame())
				//					StackFrameViewer.this.setBackgroundColor(Constants.Colors.INST_POINTER);
				setBorder(new LineBorder(Constants.Colors.INST_POINTER, 2));
			else
				setBorder(new LineBorder(ColorConstants.lightGray, 2));
			//					StackFrameViewer.this.setBackgroundColor(Constants.Colors.VIEW_BACKGROUND);
		}
	}

	private void add(IVariableModel<?> v) {
		PandionJFigure<?> figure = figProvider.getFigure(v);
		add(figure);

		layout.setConstraint(figure, Constants.RIGHT_ALIGN);

		if(v instanceof IReferenceModel) {
			IReferenceModel ref = (IReferenceModel) v;
			IEntityModel target = ref.getModelTarget();
			PandionJFigure<?> targetFig = null;
			if(!target.isNull())
				targetFig = objectContainer.addObject(target);
			addPointer((ReferenceFigure) figure, ref, target, targetFig);
			objectContainer.updateIllustration(ref, null);
		}
	}

	private void addPointer(ReferenceFigure figure, IReferenceModel ref, IEntityModel target, PandionJFigure<?> targetFig) {
		PolylineConnection pointer = new PolylineConnection();
		pointer.setVisible(!target.isNull());
		pointer.setSourceAnchor(figure.getAnchor());
		if(target.isNull())
			pointer.setSourceAnchor(figure.getAnchor());
		else
			pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));
		RuntimeViewer.addPointerDecoration(target, pointer);
		addPointerObserver(ref, pointer);
		rootPane.add(pointer);
		pointerMap.put(ref, pointer);
	}

	private void addPointerObserver(IReferenceModel ref, PolylineConnection pointer) {
		ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
			@Override
			public void update(IEntityModel arg) {
				IEntityModel target = ref.getModelTarget();
				pointer.setVisible(!target.isNull());
				if(!target.isNull()) {
					PandionJFigure<?> targetFig = objectContainer.addObject(target);
					pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));
					RuntimeViewer.addPointerDecoration(target, pointer);
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
}
