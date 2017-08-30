package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IReferenceModel;

class ReferenceLabel extends Figure {
	private IReferenceModel ref;
	
	public ReferenceLabel(IReferenceModel ref) {
		this.ref = ref;
		ref.registerDisplayObserver((o) -> repaint());
	}
	
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		return new Dimension(Constants.POSITION_WIDTH/2, Constants.POSITION_WIDTH/2);
	}
	
	@Override
	protected void paintFigure(Graphics g) {
		super.paintFigure(g);
		Rectangle r = getBounds();
		Rectangle c = new Rectangle(r.getLocation().getTranslated(0, r.height/4), new Dimension(r.width/2, r.height/2));

		g.setBackgroundColor(ColorConstants.white);
		g.fillRectangle(c);

		g.setForegroundColor(ColorConstants.black);
		g.drawRectangle(c);

		g.setBackgroundColor(ColorConstants.black);
		Point center = new Point(c.x + c.width/2, c.y + c.height/2);
		g.fillOval(center.x-3, center.y-3, 7, 7);

		if(ref.getModelTarget().isNull()) {
			Point dest = center.getTranslated(20, 0);
			g.drawLine(center, dest);
			g.drawLine(dest.getTranslated(-3, 5), dest.getTranslated(3, -5));
		}
	}
}