package pt.iscte.pandionj;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.draw2d.geometry.Dimension;
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
import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.figures.AbstractArrayFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.PandionJFigure.Extension;
import pt.iscte.pandionj.figures.PositionAnchor;
import pt.iscte.pandionj.model.ModelObserver;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.model.StackFrameModel;

public class RuntimeViewer extends Composite {
	private static final int GAP = 100;

	private FigureProvider figProvider;
	private Figure rootFig;
	private StackFigure stackFig;
	private ObjectContainer objectFig;
	private LightweightSystem lws;
	private GridLayout rootGrid;
	private ScrolledComposite scroll;
	private Canvas canvas;


	public RuntimeViewer(Composite parent) {
		super(parent, SWT.BORDER);
		setLayout(new FillLayout());
		setBackground(Constants.Colors.VIEW_BACKGROUND);
		scroll = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setBackground(Constants.Colors.VIEW_BACKGROUND);
		canvas = new Canvas(scroll, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroll.setContent(canvas);

		rootFig = new Figure();
		rootFig.setBackgroundColor(ColorConstants.white);
		rootGrid = new GridLayout(2, false);
		rootGrid.horizontalSpacing = GAP;
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
	}

	private void refresh(RuntimeModel model, RuntimeModel.Event<?> event) {
		if(event.type == RuntimeModel.Event.Type.NEW_STACK)
			rebuildStack(model);
		else if(event.type == RuntimeModel.Event.Type.NEW_FRAME)
			stackFig.addFrame((StackFrameModel) event.arg, false);

		stackFig.getLayoutManager().layout(stackFig);
		updateLayout();
	}

	private void updateLayout() {
		org.eclipse.swt.graphics.Point prev = canvas.getSize();
		Dimension size = rootFig.getPreferredSize();
		canvas.setSize(size.width, size.height);
		canvas.layout();
		if(size.width > prev.y)
			scroll.setOrigin(size.width, size.height);

	}

	private void rebuildStack(RuntimeModel model) {
		//		for (Object object : rootFig.getChildren()) {
		//			if(object instanceof PolylineConnection)
		//				
		//		}
		stackFig.removeAll();
		objectFig.removeAll();
		IStackFrameModel staticVars = model.getStaticVars();
		stackFig.addFrame(staticVars, true);
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
			GridLayout gridLayout = new GridLayout(1, true);
			gridLayout.verticalSpacing = Constants.OBJECT_PADDING*2;
			setLayoutManager(gridLayout);
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

		Map<IReferenceModel, PolylineConnection> pointerMap;

		ObjectContainer() {
			setBackgroundColor(ColorConstants.white);
			setOpaque(true);
			setLayoutManager(new GridLayout(1, true));
			pointerMap = new HashMap<>();
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

		@Override
		public void removeAll() {
			for (PolylineConnection conn : pointerMap.values())
				rootFig.remove(conn);

			pointerMap.clear();
			super.removeAll();
		}

		PandionJFigure<?> addObject(IEntityModel e) {
			PandionJFigure<?> fig = figProvider.getFigure(e);
			if(!containsChild(fig)) {
				if(e instanceof IArrayModel && ((IArrayModel<?>) e).isReferenceType() && fig instanceof ArrayReferenceFigure) {
					Extension ext = new Extension(fig, e);
					ext.setLayoutManager(new GridLayout(2, false));

					Figure container2d = new Figure();
					container2d.setLayoutManager(new GridLayout(1, false));
					//					container2d.setOpaque(true);
					//					container2d.setBackgroundColor(ColorConstants.blue);
					ext.add(container2d);

					IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) e;
					Iterator<Integer> it = a.getValidModelIndexes();
					while(it.hasNext()) {
						Integer next = it.next();
						add2dElement(container2d, fig, a, next);
					}
					add(ext);
				}
				else {					
					add(fig);
				}
				getLayoutManager().layout(this);
			}
			return fig;
		}

		private void add2dElement(Figure container, PandionJFigure<?> targetFig, IArrayModel<IReferenceModel> a, int i) {
			IReferenceModel e = a.getElementModel(i);
			IEntityModel eTarget = e.getModelTarget();
			PandionJFigure<?> eTargetFig = null;
			if(!eTarget.isNull()) {
				eTargetFig = figProvider.getFigure(eTarget);
				container.add(eTargetFig);
			}
			addPointer2D((ArrayReferenceFigure) targetFig, e, i, eTarget, eTargetFig, container);
		}


		private void addPointer2D(ArrayReferenceFigure figure, IReferenceModel ref, int index, IEntityModel target, PandionJFigure<?> targetFig, Figure container) {
			PolylineConnection pointer = new PolylineConnection();
			pointer.setVisible(!target.isNull());
			pointer.setSourceAnchor(figure.getAnchor(index));
			if(target.isNull())
				pointer.setTargetAnchor(figure.getAnchor(index));
			else
				pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));

			addPointerDecoration(target, pointer);
			addPointerObserver(ref, pointer, container);
			rootFig.add(pointer);
			pointerMap.put(ref, pointer);
		}

		private void addPointerObserver(IReferenceModel ref, PolylineConnection pointer, Figure container) {
			ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
				@Override
				public void update(IEntityModel arg) {
					IEntityModel target = ref.getModelTarget();
					pointer.setVisible(!target.isNull());
					if(!target.isNull()) {
						PandionJFigure<?> figure = figProvider.getFigure(target);
						if(!containsChild(figure))
							container.add(figure);
						pointer.setTargetAnchor(new PositionAnchor(figure, PositionAnchor.Position.LEFT));
						addPointerDecoration(target, pointer);
						container.getLayoutManager().layout(container);
					}
				}
			});
		}

		void updateIllustration(IReferenceModel v) {
			IEntityModel target = v.getModelTarget();
			PandionJFigure<?> fig = getDeepChild(target);
			if(fig != null) {
				if(handleIllustration(v, fig.getInnerFigure())) {
					//								xyLayout.setConstraint(fig, new Rectangle(fig.getBounds().getLocation(), fig.getPreferredSize()));
					//								xyLayout.layout(StackFrameViewer.this);
				}

				if(target instanceof IArrayModel && ((IArrayModel) target).isReferenceType()) {
					IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) target;
					for (IReferenceModel e : a.getModelElements())
						updateIllustration(e);
				}
			}
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

		private 	boolean containsChild(IFigure child) {
			for (Object object : getChildren()) {
				if(object == child)
					return true;
			}
			return false;
		}

		private PandionJFigure<?> getDeepChild(IEntityModel e) {
			return getDeepChildRef(this, e);
		}

		private PandionJFigure<?> getDeepChildRef(IFigure f, IEntityModel e) {
			for (Object object : f.getChildren()) {
				if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == e)
					return (PandionJFigure<?>) object;

				PandionJFigure<?> ret = getDeepChildRef((IFigure) object, e);
				if(ret != null)
					return ret;
			}
			return null;
		}
	}

}
