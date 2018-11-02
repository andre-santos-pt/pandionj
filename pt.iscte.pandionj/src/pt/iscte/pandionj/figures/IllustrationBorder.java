package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.extensibility.PandionJConstants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.extensibility.PandionJConstants.ARROW_EDGE;
import static pt.iscte.pandionj.extensibility.PandionJConstants.POSITION_WIDTH;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.BoundType;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.IBound;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public class IllustrationBorder implements Border {

	private static final int BAR_HEIGHT = 100;

	private static final int EXTRA = 20;

	private final AbstractArrayFigure<?> arrayFigure;
	private final int N;
	private final boolean horizontal;

	private final Collection<IArrayIndexModel> vars;
	private final Collection<IArrayIndexModel> fixedVars;

	private final Rectangle firstPositionBounds;
	private final Rectangle lastPositionBounds;
	private final Rectangle firstLabelBounds;
	private final Rectangle lastLabelBounds;

	//	private ExceptionType exception;
	private Integer outOfBoundsAccess;

	public IllustrationBorder(IReferenceModel ref, AbstractArrayFigure<?> arrayFigure, Integer outOfBoundsAccess) {
		this.arrayFigure = arrayFigure;
		//		this.exception = exception;
		this.outOfBoundsAccess = outOfBoundsAccess;

		N = arrayFigure.getModel().getLength();
		horizontal = arrayFigure instanceof ArrayPrimitiveFigure;
		vars = ref.getIndexVars();
		for(IArrayIndexModel v : vars)
			v.registerDisplayObserver((a) -> arrayFigure.repaint());

		fixedVars = ref.getFixedIndexes();

		firstPositionBounds = arrayFigure.getPositionBounds(0, horizontal);
		lastPositionBounds = arrayFigure.getPositionBounds(N - 1, horizontal);
		firstLabelBounds = arrayFigure.getLabelBounds(0);
		lastLabelBounds = arrayFigure.getLabelBounds(N - 1);
	}


	@Override
	public Insets getInsets(IFigure figure) {
		return getInsets(figure, horizontal);
	}

	public static Insets getInsets(IFigure figure, boolean horizontal) {
		if(horizontal) {
			int out = POSITION_WIDTH + ARRAY_POSITION_SPACING * 2;
			return new Insets(0, out, EXTRA, out);
		}
		else {
			int out = PandionJConstants.POSITION_WIDTH + PandionJConstants.ARRAY_POSITION_SPACING * 2;
			return new Insets(out, EXTRA, out, 0);
		}
	}

	@Override
	public Dimension getPreferredSize(IFigure figure) {
		return new Dimension();
	}

	@Override
	public boolean isOpaque() {
		return false;
	}

	@Override
	public void paint(IFigure figure, Graphics g, Insets insets) {
		final Dimension POSITION_DIM = new Dimension(firstPositionBounds.width, PandionJConstants.POSITION_WIDTH);

		drawOutOfBoundsPositions(figure, g);

		Font font = FontManager.getFont(PandionJConstants.VAR_FONT_SIZE-4);
		g.setFont(font);
		for(IArrayIndexModel f : fixedVars) {
			int i = f.getCurrentIndex();
			String varName = f.getName();
			if(i >= N)
				varName = varName + "=" + i;
			int textWidth = FigureUtilities.getTextWidth(varName, font);
			//			Rectangle bounds = arrayFigure.getPositionBounds(i, horizontal);
			//			g.setLineStyle(SWT.LINE_DOT);
			//			g.setLineWidth(1);
			//			g.drawRectangle(bounds.getExpanded(new Insets(1)));
			//			Point from = bounds.getLocation().getTranslated(pWidth/2 - textWidth/2, y - EXTRA + Constants.ARRAY_POSITION_SPACING*2);
			Point from = getIndexLocation(i);
			if(horizontal)
				from.translate(-textWidth/2, 0);
			else
				from.translate(-Math.min(textWidth/2, PandionJConstants.POSITION_WIDTH_V), 0);

			g.setForegroundColor(PandionJConstants.Colors.CONSTANT);
			g.drawText(varName, from);
		}

		font = FontManager.getFont(PandionJConstants.VAR_FONT_SIZE);
		g.setFont(font);
		setIllustrationStyle(g);

		for(IArrayIndexModel v : vars) {
			int i = v.getCurrentIndex();
			String varName = v.getName();

			int textWidth = FigureUtilities.getTextWidth(varName, font);
			//			Point from = arrayFigure.getPositionBounds(i, horizontal).getLocation().getTranslated(0,  firstPositionBounds.height);
			Point from = getIndexLocation(i);
			if(horizontal)
				from.translate(-textWidth/2, 0);
			else
				from.translate(-Math.min(textWidth/2, PandionJConstants.POSITION_WIDTH_V), -PandionJConstants.POSITION_WIDTH_V);

			g.drawText(varName, from);
			drawHistory(g, v, PandionJConstants.Colors.ILLUSTRATION);

			IBound bound = v.getBound();
			Integer boundVal =  bound == null ? null : bound.getValue();

			boolean directionOK = false;

			if(bound != null) {
				if(boundVal == null)
					continue; // because of asynchronous evaluation

				boolean incDirection = v.getDirection() == Direction.FORWARD && boundVal >= i;
				boolean decDirection = v.getDirection() == Direction.BACKWARD && boundVal <= i;
				directionOK = incDirection || decDirection;

				if(boundVal != i && directionOK) {
					int space = v.getDirection() == Direction.FORWARD ? ARRAY_POSITION_SPACING : -ARRAY_POSITION_SPACING;
					Point arrowFrom = getIndexLocation(i).translate(0, 2);
					if(horizontal) 
						arrowFrom.translate(textWidth/2 + space, EXTRA);
					else {
						if(decDirection)
							arrowFrom.translate(0, -POSITION_WIDTH/2);
						else
							arrowFrom.translate(0, ARRAY_POSITION_SPACING*2);
					}

					Point to = getIndexLocation(boundVal);
					if(horizontal)
						to.translate(0, -firstPositionBounds.width/2);

					drawArrow(g, arrowFrom, getPointDependingOnOrientation(arrowFrom, to));
				}

				if(directionOK)
					drawBar(g, boundVal, v.getBound(), v.getDirection());
			}

			if(bound == null || !directionOK) {
				Point arrowFrom = from.getTranslated(textWidth/2, EXTRA);
				if(v.getDirection() == Direction.FORWARD) {
					Point to = getPointDependingOnOrientation(arrowFrom, arrowFrom.getTranslated(POSITION_DIM.width/2 , 0));
					drawArrow(g, arrowFrom, to);
				}
				else if(v.getDirection() == Direction.BACKWARD) {
					Point to = getPointDependingOnOrientation(arrowFrom, arrowFrom.getTranslated(-(POSITION_DIM.width/2), 0));
					drawArrow(g, arrowFrom, to);
				}
			}
		}
	}

	private Point getPointDependingOnOrientation(Point from, Point to){
		if(horizontal){
			return new Point(to.x, from.y);
		}else{
			return new Point(from.x, to.y);
		}
	}

	private void drawHistory(Graphics g, IArrayIndexModel v, Color color) {
		g.setBackgroundColor(color);
		List<String> history = v.getHistory();
		for (int j = 0; j < history.size()-1; j++) {
			Integer i = Integer.parseInt(history.get(j));
			Point p = getIndexLocation(i);

			if(horizontal)
				p = p.translate(0, EXTRA);

			g.fillOval(p.x, p.y, PandionJConstants.ILLUSTRATION_LINE_WIDTH+1, PandionJConstants.ILLUSTRATION_LINE_WIDTH+1);
		}
	}


	private void setIllustrationStyle(Graphics g) {
		g.setLineWidth(PandionJConstants.ILLUSTRATION_LINE_WIDTH);
		g.setLineDashOffset(2.5f);
		g.setLineStyle(Graphics.LINE_SOLID);
		g.setForegroundColor(PandionJConstants.Colors.ILLUSTRATION);
	}


	private Point getIndexLocation(int index) {
		Point origin = arrayFigure.getPositionBounds(index, horizontal).getLocation();

		if(horizontal)
			origin.translate(firstPositionBounds.width/2, firstPositionBounds.height + PandionJConstants.ARRAY_POSITION_SPACING);
		else {
			origin.translate(-PandionJConstants.POSITION_WIDTH/2, firstPositionBounds.height/2);
		}
		return origin;
	}

	private void drawOutOfBoundsPositions(IFigure figure, Graphics g) {
		final Dimension rectDim = firstLabelBounds.getSize().expand(-1, -1);
		if(!horizontal)
			rectDim.setSize(new Dimension(rectDim.width/2, rectDim.height/2));

		boolean showLeft = false;	
		boolean showRight = false;
		for( IArrayIndexModel v : vars){
			if(v.getBound() == null || v.getBound().getValue() == null)
				continue;

			if(v.getCurrentIndex() < 0){
				showLeft = true;
			}else if(v.getCurrentIndex() >= N){
				showRight = true;
			}
		}

		if(outOfBoundsAccess != null) {
			showLeft = outOfBoundsAccess < 0;
			showRight = outOfBoundsAccess >= N;
		}

		//		if(exception == ExceptionType.ARRAY_INDEX_OUT_BOUNDS)
		if(outOfBoundsAccess != null)
			g.setForegroundColor(PandionJConstants.Colors.ERROR);
		else
			g.setForegroundColor(ColorConstants.black);

		if(showLeft) {
			Point origin = firstLabelBounds.getLocation();
			Point p;
			if(horizontal){
				p = new Point(origin.x - firstLabelBounds.width - PandionJConstants.ARRAY_MARGIN - 1, origin.y);
			}else{
				p = new Point(origin.x, origin.y - firstLabelBounds.height);
			}
			g.setLineStyle(Graphics.LINE_DOT);
			g.drawRectangle(new Rectangle(p, rectDim));
			if(outOfBoundsAccess != null && outOfBoundsAccess < 0) {
				if(!horizontal)
					p = p.getTranslated(-firstLabelBounds.width/2, -firstLabelBounds.width);
				drawIndexText(g, p);
			}

		}

		if(showRight) {
			Point origin = lastLabelBounds.getLocation();

			Point p;
			if(horizontal){
				p = new Point(origin.x + firstLabelBounds.width + PandionJConstants.ARRAY_MARGIN + 1, origin.y);
			}else{
				p = new Point(origin.x, origin.y + firstLabelBounds.height + ARRAY_POSITION_SPACING*2 + PandionJConstants.ARRAY_MARGIN+1);
			}

			g.setLineStyle(Graphics.LINE_DOT);
			g.drawRectangle(new Rectangle(p, rectDim));
			if(outOfBoundsAccess != null && outOfBoundsAccess >= N) {
				if(!horizontal)
					p = p.getTranslated(-firstLabelBounds.width/2, -firstLabelBounds.width);
				drawIndexText(g, p);
			}
		}

	}


	private void drawIndexText(Graphics g, Point p) {
		Font f = FontManager.getFont(PandionJConstants.INDEX_FONT_SIZE);
		String text = outOfBoundsAccess.toString();
		int x = firstLabelBounds.width/2 - FigureUtilities.getTextWidth(text, f)/2;
		Point indexP = horizontal ? p.getTranslated(x, firstLabelBounds.height) :
			p.getTranslated(0, firstLabelBounds.height); // TODO vertical mid point;
		g.setFont(f);
		g.drawText(text, indexP);
	}

	private void drawArrow(Graphics g, Point from, Point to) {
		g.setLineStyle(Graphics.LINE_SOLID);
		if(horizontal){
			int xx = from.x < to.x ? -ARROW_EDGE : ARROW_EDGE;
			g.drawLine(to, to.getTranslated(xx, -ARROW_EDGE));
			g.drawLine(to, to.getTranslated(xx, ARROW_EDGE));
		}else{
			//			to = to.translate(0, from.y < to.y ? firstLabelBounds.height : -firstLabelBounds.height);
			int xx = from.y < to.y ? -ARROW_EDGE : ARROW_EDGE;
			g.drawLine(to, to.getTranslated(-ARROW_EDGE, xx));
			g.drawLine(to, to.getTranslated(ARROW_EDGE, xx));
		}
		g.drawLine(from, to);
	}

	private void drawBar(Graphics g, Integer boundVal, IBound bound, Direction direction) {
		if(direction != Direction.NONE) {
			Point origin = getIndexLocation(boundVal);

			int s;
			boolean b = direction == Direction.FORWARD && bound.getType() == BoundType.CLOSE ||
					direction == Direction.BACKWARD && bound.getType() == BoundType.OPEN;

			if(horizontal){
				s = firstLabelBounds.width/2 + PandionJConstants.ARRAY_POSITION_SPACING; 
				if(!b)
					s = -s;
			}else{ 
				s = firstPositionBounds.height/2 + PandionJConstants.ARRAY_POSITION_SPACING;
				if(!b)
					s = -s;
			}

			origin.translate(horizontal ? s : 0, horizontal ? 0 : s);

			Point from = origin.getTranslated(horizontal ? 0 : -BAR_HEIGHT, horizontal ? -BAR_HEIGHT : 0);; 

			Point to = origin.getTranslated(horizontal ? 0 : BAR_HEIGHT, horizontal ? BAR_HEIGHT : 0);
			setIllustrationStyle(g);
			g.drawLine(from, to);
		}
	}



}