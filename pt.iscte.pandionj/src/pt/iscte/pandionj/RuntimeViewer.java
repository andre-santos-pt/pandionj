package pt.iscte.pandionj;

import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.figures.AbstractArrayFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.PositionAnchor;
import pt.iscte.pandionj.model.ModelObserver;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.model.StackFrameModel;

public class RuntimeViewer extends Composite {
	private static final int GAP = 150;

	private FigureProvider figProvider;
	private Figure rootFig;
	private StackFigure stackFig;
	private ObjectContainer objectFig;

	private LightweightSystem lws;

	private GridLayout rootGrid;


	public RuntimeViewer(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());

		ScrolledComposite scroll = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinWidth(0);
		scroll.setMinHeight(100);

		Canvas canvas = new Canvas(scroll, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroll.setContent(canvas);

		rootFig = new Figure();
		rootFig.setBackgroundColor(ColorConstants.white);
		rootGrid = new GridLayout(2, false);
		rootGrid.horizontalSpacing = 50;
		rootFig.setLayoutManager(rootGrid);

		stackFig = new StackFigure();
		rootFig.add(stackFig);
		rootGrid.setConstraint(stackFig, new org.eclipse.draw2d.GridData(SWT.FILL, SWT.FILL, true, true));

		objectFig = new ObjectContainer();
		rootFig.add(objectFig);
		rootGrid.setConstraint(objectFig, new org.eclipse.draw2d.GridData(SWT.FILL, SWT.FILL, true, true));

		lws = new LightweightSystem(canvas);
		lws.setContents(rootFig);
	}

	public void setInput(RuntimeModel model) {
		//		PandionJUI.executeUpdate(() -> rebuildStack(model.getFilteredStackPath()));
		figProvider = new FigureProvider(null);


		model.registerDisplayObserver((e) -> refresh(model, e));
		//		objectFig.setInput(model);

		//		model.registerDisplayObserver((e) -> {
		//			if(e.type == RuntimeModel.Event.Type.NEW_OBJECT) {
		//				List<IReferenceModel> refs = model.findReferences((IEntityModel) e.arg);
		//				for(IReferenceModel r : refs) {
		//					for (Object object : stackFig.getChildren()) {
		//						PandionJFigure<?> refFig = ((StackFrameViewer) object).getVariableFigure(r);
		//						if(refFig != null)
		//							addPointer(r, refFig, objectFig.getEntityFigure((IEntityModel) e.arg));
		//					}	
		//				}
		//			}
		//		});

	}

	private void refresh(RuntimeModel model, RuntimeModel.Event<?> event) {
		if(event.type == RuntimeModel.Event.Type.NEW_STACK)
			rebuildStack(model);
		else if(event.type == RuntimeModel.Event.Type.NEW_FRAME)
			stackFig.addFrame((StackFrameModel) event.arg, false);

		stackFig.getLayoutManager().layout(stackFig);
	}

	private void rebuildStack(RuntimeModel model) {
		stackFig.removeAll();
		objectFig.removeAll();
		IStackFrameModel staticVars = model.getStaticVars();
		stackFig.addFrame(staticVars, true);
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

	static void addPointerDecoration(IEntityModel target, PolylineConnection pointer) {
		if(target.isNull())
			addNullDecoration(pointer);
		else
			addArrowDecoration(pointer);
	}

	private static void addArrowDecoration(PolylineConnection pointer) {
		PolylineDecoration decoration = new PolylineDecoration();
		PointList points = new PointList();
		points.addPoint(-1, -1);
		points.addPoint(0, 0);
		points.addPoint(-1, 1);
		decoration.setTemplate(points);
		decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
		decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
		decoration.setOpaque(true);
		pointer.setTargetDecoration(decoration);
	}

	private static void addNullDecoration(PolylineConnection pointer) {
		PolygonDecoration decoration = new PolygonDecoration();
		PointList points = new PointList();
		points.addPoint(0,-1); // 1
		points.addPoint(0, 1); // -1
		decoration.setTemplate(points);
		decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
		decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
		decoration.setOpaque(true);
		pointer.setTargetDecoration(decoration);	
	}


	class StackFigure extends Figure {
		public StackFigure() {
			setBackgroundColor(ColorConstants.white);
			setLayoutManager(new GridLayout(1, true));
			setOpaque(true);
		}

		void addFrame(IStackFrameModel frame, boolean invisible) {
			if(frame.getLineNumber() != -1) {
				StackFrameViewer sv = new StackFrameViewer(rootFig, frame, objectFig, invisible);
				add(sv);
				getLayoutManager().setConstraint(sv, new org.eclipse.draw2d.GridData(SWT.RIGHT, SWT.DEFAULT, true, false));
			}
		}

		@Override
		public void removeAll() {
			for (Object object : getChildren())
				((StackFrameViewer) object).clearPointers();

			super.removeAll();
		}
	}

	class ObjectContainer extends Figure {
		ObjectContainer() {
			setBackgroundColor(ColorConstants.white);
			setOpaque(true);
			setLayoutManager(new GridLayout(1, true));
		}

		void setInput(RuntimeModel model) {
			PandionJUI.executeUpdate(() -> removeAll());
			model.registerDisplayObserver((e) -> {
				if(e.type == RuntimeModel.Event.Type.NEW_STACK)
					removeAll();
				else if(e.type == RuntimeModel.Event.Type.NEW_OBJECT)
					addObject((IEntityModel) e.arg);
			});
		}

		PandionJFigure<?> addObject(IEntityModel e) {
			PandionJFigure<?> fig = figProvider.getFigure(e);
			if(!containsChild(fig)) {
				add(fig);
				getLayoutManager().layout(this);

				if(e instanceof IArrayModel && ((IArrayModel<?>) e).isReferenceType() && fig instanceof ArrayReferenceFigure) {
					IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) e;

					Iterator<Integer> it = a.getValidModelIndexes();
					while(it.hasNext()) {
						Integer next = it.next();
						add2dElement(fig, fig, a, next);
					}
				}			
			}
			return fig;
		}

		private void add2dElement(PandionJFigure<?> figure, PandionJFigure<?> targetFig, IArrayModel<IReferenceModel> a,
				int i) {
			IReferenceModel e = a.getElementModel(i);
			IEntityModel eTarget = e.getModelTarget();
			PandionJFigure<?> eTargetFig = figProvider.getFigure(eTarget);
			add(eTargetFig);
			//			addEntityFigure(e, eTargetFig, new Point(figure.getLocation().x + GAP, figure.getLocation().y * (i+1))); // TODO review spacing
			addPointer2D((ArrayReferenceFigure) targetFig, e, i, eTarget, eTargetFig);
		}


		private void addPointer2D(ArrayReferenceFigure figure, IReferenceModel ref, int index, IEntityModel target,
				PandionJFigure<?> targetFig) {
			PolylineConnection pointer = new PolylineConnection();
			pointer.setSourceAnchor(figure.getAnchor(index));
			pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));
			//			setPointerDecoration(target, pointer);
			addPointerObserver(ref, pointer);
			rootFig.add(pointer);
		}

		private void addPointerObserver(IReferenceModel ref, PolylineConnection pointer) {
			ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
				@Override
				public void update(IEntityModel arg) {
					IEntityModel target = ref.getModelTarget();
					PandionJFigure<?> targetFig = objectFig.addObject(target);
					pointer.setTargetAnchor(new ChopboxAnchor(targetFig));
					addPointerDecoration(target, pointer);
				}
			});
		}

		private 	boolean containsChild(IFigure child) {
			for (Object object : getChildren()) {
				if(object == child)
					return true;
			}
			return false;
		}
	}

}
