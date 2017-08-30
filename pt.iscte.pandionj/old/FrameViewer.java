package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel.StackEvent;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.figures.AbstractArrayFigure;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.PositionAnchor;
import pt.iscte.pandionj.model.ModelObserver;

public class FrameViewer extends Composite {
	private static final int GAP = 150;

	private FigureProvider figProvider;
	private IFigure pane;
	private LightweightSystem lws;
	private int y;
	private XYLayout xyLayout;

	public FrameViewer(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		setBackground(ColorConstants.white);

		ScrolledComposite scroll = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinWidth(0);
		scroll.setMinHeight(100);
		//		scroll.setAlwaysShowScrollBars(true);

		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Dimension dim = pane.getPreferredSize();
				scroll.setMinWidth(dim.width);
				scroll.setMinHeight(dim.height);
			}
		});


		Canvas canvas = new Canvas(scroll, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroll.setContent(canvas);

		pane = new Figure();
		pane.setBackgroundColor(ColorConstants.white);
		xyLayout = new XYLayout();
		pane.setLayoutManager(xyLayout);
		lws = new LightweightSystem(canvas);
		lws.setContents(pane);
		

		//		pane.setScale(1.2);
		y = Constants.MARGIN;
	}


	public void setModel(IStackFrameModel frame, Predicate<IVariableModel<?>> accept) {
		for(Object child : new ArrayList<Object>(pane.getChildren()))
			pane.remove((IFigure) child);

		//		rootFigure.repaint();
		xyLayout.setConstraint(pane, new GridData(GAP, GAP));
		xyLayout.layout(pane);

		y = Constants.MARGIN;

		figProvider = new FigureProvider(frame);
		Collection<IVariableModel<?>> variables = frame.getAllVariables();
		for(IVariableModel<?> v : variables) 
			if(accept.test(v))
				add(v);

		addFrameObserver(frame, accept);
		updateSize();
	}

	private void addFrameObserver(IStackFrameModel frame, Predicate<IVariableModel<?>> accept) {
		frame.registerDisplayObserver(new ModelObserver<StackEvent<?>>() {
			@Override
			public void update(StackEvent<?> event) {
				if(event != null) {
					if(event.type == StackEvent.Type.NEW_VARIABLE && accept.test((IVariableModel) event.arg)) {
						add((IVariableModel<?>) event.arg);
					}
					else if(event.type == StackEvent.Type.VARIABLE_OUT_OF_SCOPE) {
						PandionJFigure<?> toRemove = null; 

						for (Object object : pane.getChildren()) {
							if(object instanceof PandionJFigure) {
								IObservableModel<?> model = ((PandionJFigure<?>) object).getModel();
								if(event.arg == model)
									toRemove = (PandionJFigure<?>) object;
							}
						}
						if(toRemove != null) {
							Rectangle rect = (Rectangle) xyLayout.getConstraint(toRemove);
							int diff = rect.height + Constants.OBJECT_PADDING;
							pane.remove(toRemove);
							y -= diff;
							for (Object object : pane.getChildren()) {
								if(object instanceof PandionJFigure) {
									Rectangle r = (Rectangle) xyLayout.getConstraint((IFigure)object);
									if(r.y > rect.y) {
										r.y -= diff;
										xyLayout.setConstraint((IFigure) object, r);
									}
								}
							}
						}
					}

					for (IVariableModel<?> v : frame.getStackVariables()) {
						if(accept.test(v) && v instanceof IReferenceModel && ((IReferenceModel) v).hasIndexVars()) {
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
					updateSize();
				}
			}

			private void updateIllustration(IReferenceModel v) {
				IEntityModel target = v.getModelTarget();
				for (Object object : pane.getChildren()) {
					if(object instanceof PandionJFigure) {
						PandionJFigure<?> fig = (PandionJFigure<?>) object;
						IObservableModel<?> model = ((PandionJFigure<?>) object).getModel();
						if(target == model)
							if(handleIllustration(v, (PandionJFigure<?>) object)) {
								xyLayout.setConstraint(fig, new Rectangle(fig.getBounds().getLocation(), fig.getPreferredSize()));
								xyLayout.layout(pane);
							}
					}
				}
			}
		});
	}

	private void updateSize() {
		GridData prevData = (GridData) getLayoutData();
		if(prevData == null)
			prevData = new GridData(GAP, GAP);
		Dimension dim = pane.getPreferredSize().expand(Constants.MARGIN, Constants.MARGIN);
		setLayoutData(new GridData(
				Math.max(prevData.widthHint, dim.width), 
				Math.max(prevData.heightHint, dim.height)));
		xyLayout.layout(pane);
	}

	// TODO fixed values on top row
	private void add(IVariableModel<?> v) {
		PandionJFigure<?> figure = figProvider.getFigure(v);
		figure.setLocation(new Point(Constants.MARGIN, y));
		pane.add(figure);
		xyLayout.setConstraint(figure, new Rectangle(new Point(Constants.MARGIN, y), figure.getPreferredSize()));
		int h = figure.getPreferredSize().height;

		if(v instanceof IReferenceModel) {
			IReferenceModel ref = (IReferenceModel) v;
			IEntityModel target = ref.getModelTarget();
			PandionJFigure<?> targetFig = figProvider.getFigure(target);
			addEntityFigure(ref, targetFig, new Point(GAP, figure.getLocation().y));
			addPointer(figure, ref, target, targetFig);

			if(target instanceof IArrayModel && ((IArrayModel<?>) target).isReferenceType() && targetFig instanceof ArrayReferenceFigure) {
				IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) target;
				
				Iterator<Integer> it = a.getValidModelIndexes();
				while(it.hasNext()) {
					Integer next = it.next();
					System.out.println(next);
					add2dElement(figure, targetFig, a, next);
				}
				
//				int len = Math.min(a.getLength(), Constants.ARRAY_LENGTH_LIMIT);
//				for(int i = 0; i < len-1; i++) {
//					add2dElement(figure, targetFig, a, i);
//				}
				// if...
//				add2dElement(figure, targetFig, a, len-1);
			}

			int th = targetFig.getPreferredSize().height;
			if(th > h)
				h = th;
		}
		else { // ValueFigure
			v.registerDisplayObserver((a) -> {
				Rectangle r = (Rectangle) xyLayout.getConstraint(figure);
				if(r != null) {
					xyLayout.setConstraint(figure, new Rectangle(r.getLocation(), figure.getPreferredSize()));
					xyLayout.layout(figure);
				}
			});
		}
		y += h + Constants.OBJECT_PADDING;
	}


	private void add2dElement(PandionJFigure<?> figure, PandionJFigure<?> targetFig, IArrayModel<IReferenceModel> a,
			int i) {
		IReferenceModel e = a.getElementModel(i);
		IEntityModel eTarget = e.getModelTarget();
		PandionJFigure<?> eTargetFig = figProvider.getFigure(eTarget);
		addEntityFigure(e, eTargetFig, new Point(GAP*2, figure.getLocation().y * (i+1)));
		addPointer2D((ArrayReferenceFigure) targetFig, e, i, eTarget, eTargetFig);
	}

	private void addPointer(PandionJFigure<?> figure, IReferenceModel ref, IEntityModel target,
			PandionJFigure<?> targetFig) {
		PolylineConnection pointer = new PolylineConnection();
		pointer.setSourceAnchor(new ChopboxAnchor(figure));
		pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));
		setPointerDecoration(target, pointer);
		addPointerObserver(ref, pointer);
		pane.add(pointer);
	}

	private void addPointer2D(ArrayReferenceFigure figure, IReferenceModel ref, int index, IEntityModel target,
			PandionJFigure<?> targetFig) {
		PolylineConnection pointer = new PolylineConnection();
		pointer.setSourceAnchor(figure.getAnchor(index));
		pointer.setTargetAnchor(new PositionAnchor(targetFig, PositionAnchor.Position.LEFT));
		setPointerDecoration(target, pointer);
		addPointerObserver(ref, pointer);
		pane.add(pointer);
	}


	private void setPointerDecoration(IEntityModel target, PolylineConnection pointer) {
		if(target.isNull())
			addNullDecoration(pointer);
		else
			addArrowDecoration(pointer);
	}

	private void addPointerObserver(IReferenceModel ref, PolylineConnection pointer) {
		ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
			@Override
			public void update(IEntityModel arg) {
				IEntityModel target = ref.getModelTarget();
				Point prevLoc = pointer.getTargetAnchor().getOwner().getBounds().getLocation();
				PandionJFigure<?> targetFig = figProvider.getFigure(target);
				if(!containsChild(pane, targetFig)) {
					addEntityFigure(ref, targetFig, prevLoc);
					y += targetFig.getPreferredSize().height + Constants.OBJECT_PADDING;
					updateSize();
				}
				pointer.setTargetAnchor(new ChopboxAnchor(targetFig));
				setPointerDecoration(target, pointer);
			}
		});
	}

	private void addEntityFigure(IReferenceModel ref, PandionJFigure<?> targetFig, Point location) {
		pane.add(targetFig);
		handleIllustration(ref, targetFig);
		xyLayout.setConstraint(targetFig, new Rectangle(location, targetFig.getPreferredSize()));
	}

	private static boolean containsChild(IFigure f, IFigure child) {
		for (Object object : f.getChildren()) {
			if(object == child)
				return true;
		}
		return false;
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

	private void addArrowDecoration(PolylineConnection pointer) {
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

	private void addNullDecoration(PolylineConnection pointer) {
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


}
