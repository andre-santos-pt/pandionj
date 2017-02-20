package pt.iscte.pandionj;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.NodeProvider.Pointer;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;

class FigureProvider extends LabelProvider implements IFigureProvider, IConnectionStyleProvider, ISelfStyleProvider {

	@Override
	public IFigure getFigure(Object element) {
		return ((ModelElement) element).createFigure();
	}

	
	
	@Override
	public String getText(Object element) {
//		if(element instanceof EntityConnectionData)
//			return "";
//		else if(element instanceof Pointer)
//			return ((Pointer) element).refName;
//		else
//			return element.toString();
		return "";
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
		return ColorConstants.blue;
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

		if(((Pointer) element).isNull()) {
			PolygonDecoration decoration = new PolygonDecoration();
			PointList points = new PointList();
			points.addPoint(0,-2); // 1
			points.addPoint(0, 2); // -1
			decoration.setTemplate(points);
			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
			decoration.setOpaque(true);
			((PolylineConnection) connection.getConnectionFigure()).setTargetDecoration(decoration);	
		}
		else {
			PolylineDecoration decoration = new PolylineDecoration();
			PointList points = new PointList();
			points.addPoint(-2, -2);
			points.addPoint(0, 0);
			points.addPoint(-2, 2);
			decoration.setTemplate(points);
			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
			decoration.setOpaque(true);
			
			((PolylineConnection) connection.getConnectionFigure()).setTargetDecoration(decoration);
		}
	}

	
}
