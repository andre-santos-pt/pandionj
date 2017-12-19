package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.extensibility.PandionJConstants;

public class CustomChopboxAnchor extends AbstractConnectionAnchor {

	interface CenterCalculator {
		Point getCenter(Rectangle r);
	}
	
	private CenterCalculator calc;
	
	public CustomChopboxAnchor(IFigure owner, CenterCalculator calc) {
		super(owner);
		this.calc = calc;
	}

	public Point getLocation(Point b) {
		Rectangle r = Rectangle.SINGLETON;
		r.setBounds(getBox());
		r.translate(-1, -1);
		r.resize(1, 1);

		
		double gap = PandionJConstants.POSITION_WIDTH*2;
		getOwner().translateToAbsolute(r);
		
		Point c = calc.getCenter(r);
		double d = Math.sqrt(Math.pow(c.x - b.x, 2) + Math.pow(c.y - b.y, 2));
		
		
		int x = (int) Math.round(c.x - (gap * (c.x - b.x) / d));
		int y = (int) Math.round(c.y - (gap * (c.y - b.y) / d));
		
		return new Point(x, y);
	}

	protected Rectangle getBox() {
		return getOwner().getBounds();
	}

	public Point getReferencePoint() {
		Point ref = getBox().getCenter();
		getOwner().translateToAbsolute(ref);
		return ref;
	}

	public boolean equals(Object obj) {
		if (obj instanceof CustomChopboxAnchor) {
			CustomChopboxAnchor other = (CustomChopboxAnchor) obj;
			return other.getOwner() == getOwner()
					&& other.getBox().equals(getBox());
		}
		return false;
	}

	public int hashCode() {
		if (getOwner() != null)
			return getOwner().hashCode();
		else
			return super.hashCode();
	}

}
