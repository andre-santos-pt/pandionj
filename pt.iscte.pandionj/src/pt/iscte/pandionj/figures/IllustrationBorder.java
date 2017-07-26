package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.Constants.ARROW_EDGE;
import static pt.iscte.pandionj.Constants.ARROW_LINE_WIDTH;

import java.util.Collection;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.Direction;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.BoundType;
import pt.iscte.pandionj.model.ReferenceModel;

public class IllustrationBorder implements Border {

	private static final int POS = Constants.POSITION_WIDTH + Constants.ARRAY_POSITION_SPACING;
	private static final Insets EMPTY = new Insets(0, Constants.POSITION_WIDTH, 20, Constants.POSITION_WIDTH);
	private static final int BAR_HEIGHT = 100;

	private final ArrayPrimitiveFigure2 arrayFigure;

	private final Collection<IArrayIndexModel> vars;
	private final int N;

	private boolean leftBoundVisible;
	private boolean rightBoundVisible;



	public IllustrationBorder(ReferenceModel ref, ArrayPrimitiveFigure2 arrayFigure) {
		this.arrayFigure = arrayFigure;
		N = arrayFigure.getArrayLength();
		leftBoundVisible = true;
		rightBoundVisible = false;
		vars = ref.getIndexVars();

		for(IArrayIndexModel v : vars)
			v.registerDisplayObserver((o,a) -> arrayFigure.repaint());
	}


	@Override
	public Insets getInsets(IFigure figure) {
		return EMPTY;
	}

	@Override
	public Dimension getPreferredSize(IFigure figure) {
		return new Dimension();
		//		return figure.getSize().getExpanded((Constants.POSITION_WIDTH+Constants.ARRAY_POSITION_SPACING)*2, 20);
	}

	@Override
	public boolean isOpaque() {
		return true;
	}


	private static final Dimension POSITION_DIM = new Dimension(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);

	@Override
	public void paint(IFigure figure, Graphics g, Insets insets) {
		//		g.drawRectangle(figure.getBounds().getShrinked(insets));

		drawOutOfBoundsPositions(figure, g);

		Dimension dim = N == 0 ? new Dimension(10,10) : new Dimension(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);
		int pWidth = dim.width / 2;
		int y = ARRAY_POSITION_SPACING + dim.height + 20; // TODO fix
		//		int y = arrayFigure.getLocation().y + arrayFigure.getBounds().height;
		g.setLineWidth(ARROW_LINE_WIDTH);
		Font font = FontManager.getFont(Constants.VAR_FONT_SIZE);
		g.setFont(font);

		for(IArrayIndexModel v : vars) {
			int i = v.getCurrentIndex();
			String text = v.getName();

			Point from;
			if(isOutOfBounds(i)) {
				text += "=" + i;
				from = getIndexLocation(i).getTranslated(pWidth - FigureUtilities.getTextWidth(text, font)/2, y);
			}

			if(v.getBound() != null) {
				Integer boundVal =  v.getBound().getValue();
				if(boundVal == null)
					continue; // because asynchronous evaluation

				boolean right = i < boundVal;
				from = getIndexLocation(i).getTranslated(pWidth - FigureUtilities.getTextWidth(text, font)/2, y);
				Point to = getIndexLocation(boundVal).getTranslated(pWidth + (right ? -ARROW_EDGE : ARROW_EDGE), y + pWidth);
				g.drawText(text, from);
				if(boundVal != i && ((right && i <= N - 1) || (!right && i >= 0))) {
					drawArrow(from, to, pWidth, right, g);

					if(boundVal < 0 || boundVal >= N) {
						text = Integer.toString(boundVal);
						to = to.getTranslated(0, -pWidth);
						g.drawText(text, right ? to : to.getTranslated(-FigureUtilities.getTextWidth(text, font)/2, 0));
					}
				}
				//				y += pWidth;
				drawBar(g, boundVal, v.getBound().getType(), v.getDirection());
			}
			else {
				g.drawText(v.getName(), getIndexLocation(v.getCurrentIndex()).getTranslated(0, y));
				//				y += pWidth;
				if(v.getDirection() != Direction.NONE) {
					boolean right = v.getDirection() == Direction.FORWARD;
					Point origin = getIndexLocation(i).getTranslated(0, y + pWidth);
					from = right ?  origin : origin.getTranslated(pWidth, 0);
					Point to = right ? origin.getTranslated(pWidth, 0) : origin;
					drawArrow(from, to, 0, right, g);
					//					y += pWidth;
				}
			}
		}
	}


	private boolean isOutOfBounds(int i) {
		return i < 0 || i >= N;
	}

	private Point getIndexLocation(int index) {
		if(N == 0)
			return arrayFigure.getLocation();
		else if(index < 0)
			return arrayFigure.getPositionLocation(0).getTranslated(-POS, 0);
		else if(index >= N)
			return arrayFigure.getPositionLocation(N-1).getTranslated(POS, 0);
		else
			return arrayFigure.getPositionLocation(index);
	}


	private void drawOutOfBoundsPositions(IFigure figure, Graphics g) {
		if(leftBoundVisible) {		
			Point p1 = arrayFigure.getFirstPositionLocation();
			Point p2 = arrayFigure.getLocation();
			Point p = new Point(p2.x-POS, p1.y);
			//			getTranslated(-(POS + Constants.ARRAY_POSITION_SPACING*2, 0);
			//			g.setForegroundColor(error ? Constants.Colors.ERROR : ColorConstants.gray);
			g.setLineWidth(Constants.ARRAY_LINE_WIDTH);
			g.setLineDashOffset(2.5f);
			g.setLineStyle(Graphics.LINE_DASH);
			g.drawRectangle(new Rectangle(p, POSITION_DIM));
		}
		// TODO right
	}

	private void drawArrow(Point from, Point to, int pWidth, boolean right, Graphics g) {
		g.setLineStyle(Graphics.LINE_SOLID);
		Point arrowTo = to.getTranslated(right ? 0 : pWidth/2, 0);
		g.drawLine(from.getTranslated(right ? pWidth : 0, pWidth), arrowTo);
		Point a = arrowTo.getTranslated(right ? -ARROW_EDGE : ARROW_EDGE, -ARROW_EDGE);
		g.drawLine(arrowTo, a);
		a = a.getTranslated(0, ARROW_EDGE*2);
		g.drawLine(arrowTo, a);
	}

	// TODO open/close
	private void drawBar(Graphics g, Integer boundVal, BoundType boundType, Direction direction) {
		if(direction != Direction.NONE) {
			Point origin = getIndexLocation(boundVal);
			if(direction == Direction.FORWARD && boundType == BoundType.CLOSE ||
				direction == Direction.BACKWARD && boundType == BoundType.OPEN)
				origin.translate(POS, 0);
				
			Point from = origin.getTranslated(-ARRAY_POSITION_SPACING-1, -BAR_HEIGHT);
			Point to = origin.getTranslated(-ARRAY_POSITION_SPACING-1, BAR_HEIGHT);
			g.setLineWidth(ARRAY_POSITION_SPACING);
			g.setLineStyle(Graphics.LINE_DASH);
			g.drawLine(from, to);
		}
	}
}
