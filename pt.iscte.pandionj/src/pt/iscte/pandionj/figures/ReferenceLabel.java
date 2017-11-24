package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.IVariableModel.Role;

class ReferenceLabel extends Figure {
	private IReferenceModel ref;
	private Point center;
	private boolean dirty;
	private boolean error;
	private boolean isnull;
	
	public ReferenceLabel(IReferenceModel ref) {
		this.ref = ref;
		ref.registerDisplayObserver((o) -> {dirty = true; repaint();});
		center = new Point(-1,-1);
		ref.getRuntimeModel().registerDisplayObserver((e) -> {
			if(e.type == IRuntimeModel.Event.Type.STEP) {
				dirty = false;
				repaint();
			}
		});
		error = false;
		updateNull();
		ref.registerObserver((a) -> updateNull());
	}

	public void setError() {
		error = true;
	}
	
	private void updateNull() {
		isnull = ref.getModelTarget().isNull();
	}
	
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		return new Dimension(Constants.POSITION_WIDTH/2, Constants.POSITION_WIDTH/2);
	}

	@Override
	protected void paintFigure(Graphics g) {
		super.paintFigure(g);
//		if(ref.getRuntimeModel().isTerminated())
//			return; // FIXME box disapears; cache result
		
		Rectangle r = getBounds();
		Rectangle square = new Rectangle(r.getLocation().getTranslated(0, r.height/4), new Dimension(r.width/2, r.height/2));
		center = new Point(square.x + square.width/2, square.y + square.height/2);

		g.setBackgroundColor(dirty ? Constants.Colors.HIGHLIGHT : Constants.Colors.VARIABLE_BOX);
		g.fillRectangle(square);

		g.setForegroundColor(ref.getRole() == Role.FIXED_VALUE ? Constants.Colors.CONSTANT : ColorConstants.black);
		g.drawRectangle(square);

		g.setBackgroundColor(error ? Constants.Colors.ERROR : ColorConstants.black);
		g.fillOval(center.x-3, center.y-3, 7, 7);

//		if(ref.getModelTarget().isNull()) {
		if(isnull) {
			g.setForegroundColor(error ? Constants.Colors.ERROR : ColorConstants.black);
			Point dest = center.getTranslated(20, 0);
			g.drawLine(center, dest);
			g.drawLine(dest.getTranslated(-3, 5), dest.getTranslated(3, -5));
		}
	}

	public ConnectionAnchor getAnchor() {
		return new PositionAnchor(this, PositionAnchor.Position.QUARTER1);
	}

}