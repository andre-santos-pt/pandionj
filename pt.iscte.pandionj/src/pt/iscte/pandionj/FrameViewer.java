package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
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

import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure2;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.model.StackFrameModel.StackEvent;

public class FrameViewer extends Composite {
	private static final int GAP = 150;
	
	private FigureProvider2 figProvider;
	private IFigure rootFigure;
	private LightweightSystem lws;
	private int y;
	private XYLayout xyLayout;

	public FrameViewer(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());

		ScrolledComposite scroll = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinWidth(0);
		scroll.setMinHeight(100);
		scroll.setAlwaysShowScrollBars(true);

		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Dimension dim = rootFigure.getPreferredSize();
				scroll.setMinWidth(dim.width);
				scroll.setMinHeight(dim.height);
			}
		});


		Canvas canvas = new Canvas(scroll, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroll.setContent(canvas);

		rootFigure = new Figure();
		xyLayout = new XYLayout();
		rootFigure.setLayoutManager(xyLayout);
		rootFigure.setSize(400, 400);
		lws = new LightweightSystem(canvas);
		lws.setContents(rootFigure);

		y = Constants.MARGIN;
	}

//	private class Area extends Figure {
//
//		@Override
//		public Dimension getPreferredSize(int wHint, int hHint) {
//			int maxW = Constants.MARGIN;
//			int maxH = Constants.MARGIN;
//
//			for (Object object : getChildren()) {
//				Rectangle r = ((IFigure) object).getBounds();
//				if(r.x > maxW)
//					maxW = r.x;
//				if(r.y > maxH)
//					maxH = r.y;
//			}
//			return xyLayout.getPreferredSize(rootFigure, wHint, hHint);
//			//			return xyLayout.getPreferredSize(rootFigure, SWT.DEFAULT, SWT.DEFAULT).getExpanded(Constants.MARGIN*2, Constants.MARGIN);
//		}
//	}



	public void setModel(IStackFrameModel frame, Predicate<IVariableModel> accept) {
		for(Object child : new ArrayList<>(rootFigure.getChildren()))
			rootFigure.remove((IFigure) child);

		rootFigure.repaint();

		y = Constants.MARGIN;

		figProvider = new FigureProvider2(frame);
		Collection<IVariableModel> variables = frame.getAllVariables();
		for(IVariableModel v : variables) 
			if(accept.test(v))
				add(v);

		addFrameObserver(frame, accept);
		updateSize();
	}

	private void addFrameObserver(IStackFrameModel frame, Predicate<IVariableModel> accept) {
		frame.registerDisplayObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				StackEvent event = (StackEvent) arg;

				if(event != null) {
					if(event.type == StackEvent.Type.NEW_VARIABLE && accept.test(event.variable)) {
						add(event.variable);
					}
					else if(event.type == StackEvent.Type.VARIABLE_OUT_OF_SCOPE) {
						PandionJFigure<?> toRemove = null; 

						for (Object object : rootFigure.getChildren()) {
							if(object instanceof PandionJFigure) {
								IObservableModel model = ((PandionJFigure<?>) object).getModel();
								if(event.variable == model)
									toRemove = (PandionJFigure<?>) object;
							}
						}
						if(toRemove != null) {
							Rectangle rect = (Rectangle) xyLayout.getConstraint(toRemove);
							int diff = rect.height + Constants.OBJECT_PADDING;
							rootFigure.remove(toRemove);
							y -= diff;
							for (Object object : rootFigure.getChildren()) {
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
					
					for (IVariableModel v : frame.getStackVariables()) {
						if(accept.test(v) && v instanceof IReferenceModel && ((IReferenceModel) v).hasIndexVars()) {
							for (Object object : rootFigure.getChildren()) {
								if(object instanceof PandionJFigure) {
									IObservableModel model = ((PandionJFigure<?>) object).getModel();
									if(((IReferenceModel) v).getModelTarget() == model)
										handleIllustration((IReferenceModel) v, (PandionJFigure<?>) object);								
								}
							}
						}
					}
					updateSize();
					
				}
				
				

//				for (Object f : rootFigure.getChildren()) {
//					IFigure fig = (IFigure) f;
//					Rectangle r = (Rectangle) xyLayout.getConstraint(fig);
//					if(r != null)
//						xyLayout.setConstraint(fig, new Rectangle(r.getLocation(), fig.getPreferredSize()));
//				}

				
			}
		});
	}
	
	private void updateSize() {
		Dimension dim = rootFigure.getPreferredSize();
		setLayoutData(new GridData(dim.width, dim.height));
		xyLayout.layout(rootFigure);
//		requestLayout();
	}

	private void add(IVariableModel v) {
		PandionJFigure<?> figure = figProvider.getFigure(v);
		figure.setLocation(new Point(Constants.MARGIN, y));
		rootFigure.add(figure);
		xyLayout.setConstraint(figure, new Rectangle(new Point(Constants.MARGIN, y), figure.getPreferredSize()));
		int h = figure.getPreferredSize().height;

		if(v instanceof IReferenceModel) {
			IReferenceModel ref = (IReferenceModel) v;
			IEntityModel target = ref.getModelTarget();
			PandionJFigure<?> targetFig = figProvider.getFigure(target);
			addEntityFigure(ref, figure, targetFig, new Point(GAP, figure.getLocation().y));
			PolylineConnection pointer = new PolylineConnection();
			pointer.setSourceAnchor(new ChopboxAnchor(figure));
			pointer.setTargetAnchor(new ChopboxAnchor(targetFig));
			if(target.isNull())
				addNullDecoration(pointer);
			else
				addArrowDecoration(pointer);
			
			rootFigure.add(pointer);

			v.registerDisplayObserver(new Observer() {

				@Override
				public void update(Observable o, Object arg) {
					IEntityModel target = ref.getModelTarget();
					PandionJFigure<?> targetFig = figProvider.getFigure(target);
					if(!containsChild(rootFigure, targetFig)) {
						addEntityFigure(ref, figure, targetFig, new Point(GAP, y));
						y += targetFig.getPreferredSize().height + Constants.OBJECT_PADDING;
						updateSize();
					}
					
					pointer.setTargetAnchor(new ChopboxAnchor(targetFig));
					if(target.isNull())
						addNullDecoration(pointer);
					else
						addArrowDecoration(pointer);
				}
			});

			int th = targetFig.getPreferredSize().height;
			if(th > h)
				h = th;
		}
		else { // ValueFigure
			v.registerDisplayObserver((o,a) -> {
				Rectangle r = (Rectangle) xyLayout.getConstraint(figure);
				if(r != null) {
					xyLayout.setConstraint(figure, new Rectangle(r.getLocation(), figure.getPreferredSize()));
					xyLayout.layout(figure);
				}
			});
		}
		y += h + Constants.OBJECT_PADDING;
	}

	private void addEntityFigure(IReferenceModel ref, PandionJFigure<?> figure, PandionJFigure<?> targetFig, Point location) {
		rootFigure.add(targetFig);
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

	private void handleIllustration(IReferenceModel reference, IFigure targetFig) {
		if(targetFig instanceof ArrayPrimitiveFigure2 &&  reference.hasIndexVars()) {
			IllustrationBorder b = new IllustrationBorder(reference, (ArrayPrimitiveFigure2) targetFig);
			targetFig.setBorder(b);
		}
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
