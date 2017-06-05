package pt.iscte.pandionj;


import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.NodeProvider.Pointer;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.BaseFigure;
import pt.iscte.pandionj.model.ArrayPrimitiveModel;
import pt.iscte.pandionj.model.ArrayReferenceModel;
import pt.iscte.pandionj.model.ModelElement;

class FigureProvider extends LabelProvider implements IFigureProvider, IConnectionStyleProvider, ISelfStyleProvider {

	private Graph graph;

	public FigureProvider(Graph graph) {
		this.graph = graph;
	}

	@Override
	public IFigure getFigure(Object element) {
		ModelElement<?> model = (ModelElement<?>) element;
		return model.createFigure(graph);		
	}



	@Override
	public String getText(Object e) {
		//		if(element instanceof EntityConnectionData)
		//			return "";
		//		else if(element instanceof Pointer)
		//			return ((Pointer) element).refName;
		//		else
		//			return element.toString();
		//		if(e instanceof Pointer && !((Pointer) e).isTopLevel())
		return null;
	}

	@Override
	public IFigure getTooltip(Object entity) {
		if(entity instanceof Pointer)
			return new Label(((Pointer) entity).refName);
		else
			return new Label(entity.toString());
	}

	@Override
	public int getConnectionStyle(Object rel) {
		return ZestStyles.CONNECTIONS_SOLID;
	}

	@Override
	public Color getColor(Object rel) {
		return ColorConstants.black;
	}

	@Override
	public Color getHighlightColor(Object rel) {
		return Constants.SELECT_COLOR;
	}

	@Override
	public int getLineWidth(Object rel) {
		return Constants.ARROW_LINE_WIDTH;
	}

	@Override
	public void selfStyleNode(Object element, GraphNode node) {
		node.getNodeFigure().setSize(-1, -1);
	}

	@Override
	public void selfStyleConnection(Object element, GraphConnection connection) {
		PolylineConnection fig = (PolylineConnection) connection.getConnectionFigure();
		Pointer pointer = (Pointer) element;
		
		if(pointer.isNull()) {
			PolygonDecoration decoration = new PolygonDecoration();
			PointList points = new PointList();
			points.addPoint(0,-1); // 1
			points.addPoint(0, 1); // -1
			decoration.setTemplate(points);
			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
			decoration.setOpaque(true);
			fig.setTargetDecoration(decoration);	
		}
		else {
			PolylineDecoration decoration = new PolylineDecoration();
			PointList points = new PointList();
			points.addPoint(-1, -1);
			points.addPoint(0, 0);
			points.addPoint(-1, 1);
			decoration.setTemplate(points);
			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
			decoration.setOpaque(true);
			fig.setTargetDecoration(decoration);

			//			fig.setSourceAnchor(new PositionAnchor(connection.getSource().getNodeFigure(), Position.RIGHT));
			//			fig.setTargetAnchor(new PositionAnchor(connection.getDestination().getNodeFigure(), Position.LEFT));
		}

		IFigure sFig = ((BaseFigure) connection.getSource().getNodeFigure()).innerFig;
		
		if(sFig instanceof ArrayReferenceFigure) {
			// TODO anchor
//			String refName = ((Pointer) element).refName;
//			refName = refName.substring(1, refName.length()-1);
//			fig.setSourceAnchor(((ArrayReferenceFigure) sFig).getAnchor(Integer.parseInt(refName)));
		}
		else if(sFig instanceof ArrayPrimitiveFigure) {
			fig.setTargetAnchor(new PositionAnchor(connection.getDestination().getNodeFigure(), Position.LEFT));
		}
		
		fig.setVisible(pointer.source.isWithinScope() && pointer.target.isWithinScope());
	}

	enum Position {
		TOP {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y);
			}
		},
		RIGHT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width, r.y + r.height/2);
			}
		},
		BOTTOM {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y + r.height);
			}
		},
		LEFT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x, r.y + r.height/2);
			}
		};

		abstract Point getPoint(org.eclipse.draw2d.geometry.Rectangle r);
	}

	private class PositionAnchor extends AbstractConnectionAnchor {

		private Position position;

		public PositionAnchor(IFigure fig, Position position) {
			super(fig);
			this.position = position;
		}

		@Override
		public Point getLocation(Point reference) {
			org.eclipse.draw2d.geometry.Rectangle r =  org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
			r.setBounds(getOwner().getBounds());
			r.translate(0, 0);
			r.resize(1, 1);
			getOwner().translateToAbsolute(r);
			return position.getPoint(r);
		}
	}
}
