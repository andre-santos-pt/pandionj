package pt.iscte.pandionj.figures;

import java.util.Collection;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IReferenceModel;

public class ReferenceFigure extends PandionJFigure<IReferenceModel> {

	private ReferenceLabel refLabel;
	
	public ReferenceFigure(IReferenceModel model) {
		super(model);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 3;
		layout.verticalSpacing = 0;
		setLayoutManager(layout);
		Label label = new Label(model.getName());
		if(model.isInstance())
			FontManager.setFont(label, Constants.VAR_FONT_SIZE, FontManager.Style.BOLD);
		else
			FontManager.setFont(label, Constants.VAR_FONT_SIZE);

		Collection<String> tags = model.getTags();
		if(!tags.isEmpty())
			label.setToolTip(new Label("tags: " + tags.toString()));

		add(label);
		refLabel = new ReferenceLabel(model);
		add(refLabel);
		layout.setConstraint(refLabel, new GridData(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH));
//		setBorder(new NullBorder());
	}

	private class RefLabel extends Figure {
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
			
			if(getModel().getModelTarget().isNull())
				g.drawLine(center, center.getTranslated(30, 0));
		}
	}
	
	public ConnectionAnchor getAnchor() {
		return new PositionAnchor(refLabel, PositionAnchor.Position.CENTER);
	}

	private class NullBorder implements Border {
		
		@Override
		public Insets getInsets(IFigure figure) {
			return new Insets(0, 0, 0, 50);
		}
		
		@Override
		public Dimension getPreferredSize(IFigure figure) {
			return new Dimension();
		}
		
		@Override
		public boolean isOpaque() {
			return true;
		}
		
		@Override
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			graphics.fillOval(new Rectangle(figure.getBounds().getLocation().getTranslated(25, 0), new Dimension(5, 10)));
			
		}
		
	}
}
