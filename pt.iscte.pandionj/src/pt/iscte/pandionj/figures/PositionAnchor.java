package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.extensibility.PandionJConstants;

public class PositionAnchor extends AbstractConnectionAnchor {
	interface PointProvider {
		Point getPoint(Rectangle r);
	}
	
	public enum Position implements PointProvider {
		TOP {
			@Override
			public Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y);
			}
		},
		RIGHT {
			@Override
			public Point getPoint(Rectangle r) {
				return new Point(r.x + r.width, r.y + r.height/2);
			}
		},
		BOTTOM {
			@Override
			public Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y + r.height);
			}
		},
		LEFT {
			@Override
			public Point getPoint(Rectangle r) { // TODO only for array
				return new Point(r.x+PandionJConstants.POSITION_WIDTH, r.y + r.height/2);
			}
		},
		TOPLEFT {
			@Override
			public Point getPoint(Rectangle r) {
				return new Point(r.x, r.y + PandionJConstants.OBJECT_PADDING);
			}
		},
		CENTER {
			@Override
			public Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y + r.height/2);
			}
		},
		QUARTER1 {
			@Override
			public Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/4, r.y + r.height/2);
			}
		};

		public abstract Point getPoint(org.eclipse.draw2d.geometry.Rectangle r);
	}

	private PointProvider provider;

	public PositionAnchor(IFigure fig, Position position) {
		this(fig, (PointProvider) position);
	}
	
	public PositionAnchor(IFigure fig, PointProvider provider) {
		super(fig);
		this.provider = provider;
	}

	@Override
	public Point getLocation(Point reference) {
		org.eclipse.draw2d.geometry.Rectangle r =  org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
		r.setBounds(getOwner().getBounds());
		r.translate(0, 0);
		r.resize(1, 1);
		getOwner().translateToAbsolute(r);
		return provider.getPoint(r);
	}
}