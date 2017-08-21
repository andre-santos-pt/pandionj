package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.Constants.ARROW_EDGE;

import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.BoundType;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.IBound;
import pt.iscte.pandionj.extensibility.IReferenceModel;

public class IllustrationBorder implements Border {

//	private static final Insets EMPTY = new Insets(0, Constants.POSITION_WIDTH, EXTRA, Constants.POSITION_WIDTH);
	private static final int BAR_HEIGHT = 100;

	private static final int EXTRA = 20;
	
	private final ArrayPrimitiveFigure2 arrayFigure;

	private final Collection<IArrayIndexModel> vars;
	private final int N;

	private final Collection<IArrayIndexModel> fixedVars;
	
	private boolean leftBoundVisible;
	private boolean rightBoundVisible;

//	private final int POS;
//	private final Dimension POSITION_DIM;


	public IllustrationBorder(IReferenceModel ref, ArrayPrimitiveFigure2 arrayFigure) {
		this.arrayFigure = arrayFigure;
		N = arrayFigure.getNumberOfPositions();
		leftBoundVisible = true;
		rightBoundVisible = false;
		vars = ref.getIndexVars();

		for(IArrayIndexModel v : vars)
			v.registerDisplayObserver((o,a) -> arrayFigure.repaint());

		fixedVars = ref.getFixedIndexes();
		
//		int w = arrayFigure.getPositionBounds(0).width;
//		POS = w + Constants.ARRAY_POSITION_SPACING;
//		POSITION_DIM = new Dimension(w, Constants.POSITION_WIDTH);
	}


	@Override
	public Insets getInsets(IFigure figure) {
//		return new Insets(0, POSITION_DIM.width, EXTRA, POSITION_DIM.width);
		return new Insets(0, Constants.POSITION_WIDTH, EXTRA, Constants.POSITION_WIDTH);
	}

	@Override
	public Dimension getPreferredSize(IFigure figure) {
//		int w = 0;
//		if(leftBoundVisible)
//			w += POSITION_DIM.width + ARRAY_POSITION_SPACING;
//		if(rightBoundVisible)
//			w += POSITION_DIM.width + ARRAY_POSITION_SPACING;
		
//		return figure.getSize().getExpanded(w, EXTRA);
		return new Dimension();
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	@Override
	public void paint(IFigure figure, Graphics g, Insets insets) {
		int w = arrayFigure.getPositionBounds(0).width;
		int POS = w + Constants.ARRAY_POSITION_SPACING;
		Dimension POSITION_DIM = new Dimension(w, Constants.POSITION_WIDTH);
		
		drawOutOfBoundsPositions(figure, g, POS, POSITION_DIM);

		int pWidth = POSITION_DIM.width;
		int y = ARRAY_POSITION_SPACING + POSITION_DIM.height + EXTRA;

		Font font = FontManager.getFont(Constants.VAR_FONT_SIZE-4);
		g.setFont(font);
		for(IArrayIndexModel f : fixedVars) {
			int i = f.getCurrentIndex();
			String varName = f.getName();
			int textWidth = FigureUtilities.getTextWidth(varName, font);
			Rectangle bounds = arrayFigure.getPositionBounds(i);
			g.setLineStyle(SWT.LINE_DOT);
			g.setLineWidth(1);
			g.drawRectangle(bounds.getExpanded(new Insets(1)));
			Point from = bounds.getLocation().getTranslated(pWidth/2 - textWidth/2, y - EXTRA + Constants.ARRAY_POSITION_SPACING);
			g.setForegroundColor(Constants.Colors.CONSTANT);
			g.drawText(varName, from);
		}
			
		font = FontManager.getFont(Constants.VAR_FONT_SIZE);
		g.setFont(font);
		setIllustrationStyle(g);

		for(IArrayIndexModel v : vars) {
			int i = v.getCurrentIndex();
			String varName = v.getName();

			if(isOutOfBounds(i))
				varName += "=" + i;

			int textWidth = FigureUtilities.getTextWidth(varName, font);
			Point from = getIndexLocation(i, POS).getTranslated(pWidth/2 - textWidth/2, y);

			g.drawText(varName, from);
			drawHistory(g, v);
			
			IBound bound = v.getBound();
			Integer boundVal =  bound == null ? null : bound.getValue();

			boolean directionOK = false;

			if(bound != null) {
				if(boundVal == null)
					continue; // because of asynchronous evaluation

				directionOK = v.getDirection() == Direction.FORWARD && boundVal >= i ||
						v.getDirection() == Direction.BACKWARD && boundVal <= i;

				if(boundVal != i && directionOK) {
					int space = v.getDirection() == Direction.FORWARD ? ARRAY_POSITION_SPACING : -ARRAY_POSITION_SPACING;
					Point arrowFrom = from.getTranslated(textWidth/2 + space, EXTRA);
					Point to = new Point(getIndexLocation(boundVal, POS).x + pWidth/2, arrowFrom.y);
					drawArrow(g, arrowFrom, to);
				}
				
				if(directionOK)
					drawBar(g, boundVal, v.getBound(), v.getDirection(), POS);
			}

			if(bound == null || !directionOK) {
				Point arrowFrom = from.getTranslated(textWidth/2, EXTRA);
				if(v.getDirection() == Direction.FORWARD) {
					Point to = arrowFrom.getTranslated(POSITION_DIM.width/2, 0);
					drawArrow(g, arrowFrom, to);
				}
				else if(v.getDirection() == Direction.BACKWARD) {
					Point to = arrowFrom.getTranslated(-(POSITION_DIM.width/2), 0);
					drawArrow(g, arrowFrom, to);
				}
			}
		}
	}

	private void drawHistory(Graphics g, IArrayIndexModel v) {
		g.setBackgroundColor(ColorConstants.gray);
		List<String> history = v.getHistory();
		for (int j = 0; j < history.size()-1; j++) {
			Integer i = Integer.parseInt(history.get(j));
			Rectangle r = arrayFigure.getPositionBounds(i);
			g.fillOval(r.x + r.width/2, r.y + r.height + EXTRA, 3, 3);
		}
	}


	private void setIllustrationStyle(Graphics g) {
		g.setLineWidth(Constants.ILLUSTRATION_LINE_WIDTH);
		g.setLineDashOffset(2.5f);
		g.setLineStyle(Graphics.LINE_SOLID);
		g.setForegroundColor(Constants.Colors.ILLUSTRATION);
	}

	private boolean isOutOfBounds(int i) {
		return i < 0 || i >= N;
	}

	private Point getIndexLocation(int index, int POS) {
		if(N == 0)
			return arrayFigure.getLocation();
		else if(index < 0)
			return arrayFigure.getPositionBounds(0).getLocation().getTranslated(-POS, 0);
		else if(index >= N)
			return arrayFigure.getPositionBounds(N-1).getLocation().getTranslated(POS, 0);
		else
			return arrayFigure.getPositionBounds(index).getLocation();
	}


	private void drawOutOfBoundsPositions(IFigure figure, Graphics g, int POS, Dimension POSITION_DIM) {
		if(leftBoundVisible) {		
			Point p1 = arrayFigure.getFirstPositionBounds().getLocation();
			Point p2 = arrayFigure.getLocation();
			Point p = new Point(p2.x-POS, p1.y);
			//			getTranslated(-(POS + Constants.ARRAY_POSITION_SPACING*2, 0);
			//			g.setForegroundColor(error ? Constants.Colors.ERROR : ColorConstants.gray);
//			g.setLineWidth(Constants.ARRAY_LINE_WIDTH);
			g.setLineDashOffset(2.5f);
			g.setLineStyle(Graphics.LINE_DASH);
			g.drawRectangle(new Rectangle(p, POSITION_DIM));
		}
		// TODO out of bound positions
	}



	 private static void drawArrow(Graphics g, Point from, Point to) {
//		g.setLineStyle(Graphics.LINE_DASH);
		g.drawLine(from, to);

		g.setLineStyle(Graphics.LINE_SOLID);
		int xx = from.x < to.x ? -ARROW_EDGE : ARROW_EDGE;
		g.drawLine(to, to.getTranslated(xx, -ARROW_EDGE));
		g.drawLine(to, to.getTranslated(xx, ARROW_EDGE));
	}

	private void drawBar(Graphics g, Integer boundVal, IBound bound, Direction direction, int POS) {
		if(direction != Direction.NONE) {
			Point origin = getIndexLocation(boundVal, POS);
			if(direction == Direction.FORWARD && bound.getType() == BoundType.CLOSE ||
					direction == Direction.BACKWARD && bound.getType() == BoundType.OPEN)
				origin.translate(POS, 0);

			Point from = origin.getTranslated(-ARRAY_POSITION_SPACING/2, -BAR_HEIGHT);
			Point to = origin.getTranslated(-ARRAY_POSITION_SPACING/2, BAR_HEIGHT);
			setIllustrationStyle(g);
			g.drawLine(from, to);
		}
	}
}
