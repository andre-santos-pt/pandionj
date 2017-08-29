package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.RuntimeViewer.ObjectContainer;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel.StackEvent;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.figures.AbstractArrayFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.PositionAnchor;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.model.ModelObserver;
import pt.iscte.pandionj.model.StackFrameModel;

public class StackFrameViewer extends Figure {
	private GridLayout layout;
	private FigureProvider figProvider;
	private ObjectContainer objectFig;
	private Figure rootPane;
	private Map<IReferenceModel, PolylineConnection> pointerMap;
	private boolean invisible;
	
	public StackFrameViewer(Figure rootPane, IStackFrameModel frame, ObjectContainer objectFig, boolean invisible) {
		this.rootPane = rootPane;
		this.objectFig = objectFig;
		this.invisible = invisible;
		
		setBackgroundColor(ColorManager.getColor(240, 240, 240));
		layout = new GridLayout(1, false);
		layout.verticalSpacing = 2;
		setLayoutManager(layout);
		if(!invisible) {
			setOpaque(true);
			setBorder(new LineBorder(ColorConstants.lightGray, 1));
			Label label = new Label(frame.getInvocationExpression());
			label.setForegroundColor(Constants.Colors.CONSTANT);
			add(label);
		}
		pointerMap = new HashMap<>();
		figProvider = new FigureProvider(frame);
		for (IVariableModel<?> v : frame.getStackVariables())
			add(v);
		addFrameObserver(frame);
		updateSize();
		updateLook(frame);
	}


	private void addFrameObserver(IStackFrameModel frame) {
		frame.registerDisplayObserver(new ModelObserver<StackEvent>() {
			@Override
			public void update(StackEvent event) {
				if(event != null) {
					if(event.type == StackEvent.Type.NEW_VARIABLE) {
						add(event.variable);
					}
					else if(event.type == StackEvent.Type.VARIABLE_OUT_OF_SCOPE) {
						PandionJFigure<?> toRemove = getVariableFigure(event.variable); 
						if(toRemove != null)
							remove(toRemove);

						PolylineConnection conn = pointerMap.get(event.variable);
						if(conn != null)
							rootPane.remove(conn);
					}

					for (IVariableModel<?> v : frame.getStackVariables()) {
						if(v instanceof IReferenceModel) {
							IReferenceModel ref = (IReferenceModel) v;
							IEntityModel target = ref.getModelTarget();
							updateIllustration(ref);
							if(target instanceof IArrayModel && ((IArrayModel) target).isReferenceType()) {
								IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) target;
								for (IReferenceModel e : a.getModelElements())
									updateIllustration(e);
							}
						}
					}
				}
				updateSize();
				updateLook(frame);
			}



			private void updateIllustration(IReferenceModel v) {
				IEntityModel target = v.getModelTarget();
				for (Object object : getChildren()) {
					if(object instanceof PandionJFigure) {
						PandionJFigure<?> fig = (PandionJFigure<?>) object;
						IObservableModel<?> model = ((PandionJFigure<?>) object).getModel();
						if(target == model)
							if(handleIllustration(v, (PandionJFigure<?>) object)) {
								//								xyLayout.setConstraint(fig, new Rectangle(fig.getBounds().getLocation(), fig.getPreferredSize()));
								//								xyLayout.layout(StackFrameViewer.this);
							}
					}
				}
			}

			
		});
	}

	private void updateLook(IStackFrameModel model) {
		if(!invisible) {
		if(model.isObsolete())
			setBorder(new LineBorder(Constants.Colors.OBSOLETE, 2));
		//					StackFrameViewer.this.setBackgroundColor(Constants.Colors.OBSOLETE);
		else if(model.isExecutionFrame())
			//					StackFrameViewer.this.setBackgroundColor(Constants.Colors.INST_POINTER);
			setBorder(new LineBorder(Constants.Colors.INST_POINTER, 3));
		else
			setBorder(new LineBorder(ColorConstants.lightGray, 1));
		//					StackFrameViewer.this.setBackgroundColor(Constants.Colors.VIEW_BACKGROUND);
		}
	}
	
	private void updateSize() {
		//		GridData prevData = (GridData) getLayoutData();
		//		if(prevData == null)
		//			prevData = new GridData(200, 100);
		//		Dimension dim = getPreferredSize().expand(Constants.MARGIN, Constants.MARGIN);
		//		setLayoutData(new GridData(
		//				Math.max(prevData.widthHint, dim.width), 
		//				Math.max(prevData.heightHint, dim.height)));
		layout.layout(this);
	}

	private void add(IVariableModel<?> v) {
		PandionJFigure<?> figure = figProvider.getFigure(v);
		add(figure);

		//		if(figure instanceof ReferenceFigure)
		layout.setConstraint(figure, new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		//		v.registerDisplayObserver((a) -> {
		//			Rectangle r = (Rectangle) xyLayout.getConstraint(figure);
		//			if(r != null) {
		//				xyLayout.setConstraint(figure, new Rectangle(r.getLocation(), figure.getPreferredSize()));
		//				xyLayout.layout(figure);
		//			}
		//			Dimension dim = figure.getPreferredSize();
		//			layout.setConstraint(figure, new GridData(dim.width, dim.height));
		//		});

		if(v instanceof IReferenceModel) {
			IReferenceModel ref = (IReferenceModel) v;
			IEntityModel target = ref.getModelTarget();
			if(!target.isNull()) {
				PandionJFigure<?> targetFig = objectFig.addObject(target);
				addPointer((ReferenceFigure) figure, ref, target, targetFig);
			}
			//
			//			if(target instanceof IArrayModel && ((IArrayModel<?>) target).isReferenceType() && targetFig instanceof ArrayReferenceFigure) {
			//				IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) target;
			//
			//				Iterator<Integer> it = a.getValidModelIndexes();
			//				while(it.hasNext()) {
			//					Integer next = it.next();
			//					System.out.println(next);
			//					add2dElement(figure, targetFig, a, next);
			//				}
			//			}
			//
			//			int th = targetFig.getPreferredSize().height;
			//			if(th > h)
			//				h = th;
			//		}
			//		else { // ValueFigure
			//			v.registerDisplayObserver((a) -> {
			//				Rectangle r = (Rectangle) xyLayout.getConstraint(figure);
			//				if(r != null) {
			//					xyLayout.setConstraint(figure, new Rectangle(r.getLocation(), figure.getPreferredSize()));
			//					xyLayout.layout(figure);
			//				}
			//			});
		}
	}

	//	public static PandionJFigure<?> getVariableFigure(Figure fig, IObservableModel<?> v) {
	//		for (Object object : fig.getChildren()) {
	//			if(object instanceof PandionJFigure) {
	//				IObservableModel<?> model = ((PandionJFigure<?>) object).getModel();
	//				if(v == model)
	//					return (PandionJFigure<?>) object;
	//			}
	//		}
	//		return null;
	//	}



	private void add2dElement(PandionJFigure<?> figure, PandionJFigure<?> targetFig, IArrayModel<IReferenceModel> a,
			int i) {
		IReferenceModel e = a.getElementModel(i);
		IEntityModel eTarget = e.getModelTarget();
		PandionJFigure<?> eTargetFig = figProvider.getFigure(eTarget);
		addEntityFigure(e, eTargetFig, new Point(100, figure.getLocation().y * (i+1)));
		addPointer2D((ArrayReferenceFigure) targetFig, e, i, eTarget, eTargetFig);
	}

	private void addPointer(ReferenceFigure figure, IReferenceModel ref, IEntityModel target,
			PandionJFigure<?> targetFig) {
		PolylineConnection pointer = new PolylineConnection();
		pointer.setSourceAnchor(figure.getAnchor());
		pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));
		RuntimeViewer.addPointerDecoration(target, pointer);
		addPointerObserver(ref, pointer);
		rootPane.add(pointer);
		pointerMap.put(ref, pointer);
	}

	private void addPointer2D(ArrayReferenceFigure figure, IReferenceModel ref, int index, IEntityModel target,
			PandionJFigure<?> targetFig) {
		PolylineConnection pointer = new PolylineConnection();
		pointer.setSourceAnchor(figure.getAnchor(index));
		pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));
		RuntimeViewer.addPointerDecoration(target, pointer);
		addPointerObserver(ref, pointer);
		rootPane.add(pointer);
	}




	private void addPointerObserver(IReferenceModel ref, PolylineConnection pointer) {
		ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
			@Override
			public void update(IEntityModel arg) {
//				rootPane.remove(pointer);
//				pointerMap.remove(ref);
				IEntityModel target = ref.getModelTarget();
				pointer.setVisible(!target.isNull());
				if(!target.isNull()) {
					PandionJFigure<?> targetFig = objectFig.addObject(target);
					pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));
					RuntimeViewer.addPointerDecoration(target, pointer);
				}
			}
		});
	}

	private void addEntityFigure(IReferenceModel ref, PandionJFigure<?> targetFig, Point location) {
		add(targetFig);
		handleIllustration(ref, targetFig);
		//		xyLayout.setConstraint(targetFig, new Rectangle(location, targetFig.getPreferredSize()));
	}


	private boolean handleIllustration(IReferenceModel reference, IFigure targetFig) {
		if(targetFig instanceof AbstractArrayFigure) {
			if(reference.hasIndexVars()) {
				IllustrationBorder b = new IllustrationBorder(reference, (AbstractArrayFigure<?>) targetFig);
				targetFig.setBorder(b);
				return true;
			}
			else
				targetFig.setBorder(null);
		}
		return false;
	} 


	public PandionJFigure<?> getVariableFigure(IVariableModel<?> v) {
		for (Object object : getChildren()) {
			if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == v)
				return (PandionJFigure<?>) object;
		}
		return null;
	}

	public List<PandionJFigure<?>> findReferences(IEntityModel arg) {
		List<PandionJFigure<?>> list = new ArrayList<>();
		for (Object object : getChildren()) {
			if(object instanceof PandionJFigure) {
				IObservableModel<?> model = ((PandionJFigure<?>) object).getModel();
				if(model instanceof IReferenceModel && ((IReferenceModel) model).getModelTarget() == arg)
					list.add((PandionJFigure<?>) object);
			}
		}
		return list;
	}

	public void clearPointers() {
		for (PolylineConnection conn : pointerMap.values())
			rootPane.remove(conn);
	}


}
