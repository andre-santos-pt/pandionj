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
import pt.iscte.pandionj.ExceptionType;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.BoundType;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.IBound;
import pt.iscte.pandionj.extensibility.IReferenceModel;

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

	private ExceptionType exception;
	
	public IllustrationBorder(IReferenceModel ref, AbstractArrayFigure<?> arrayFigure, ExceptionType exception) {
		this.arrayFigure = arrayFigure;
		this.exception = exception;
		
		N = arrayFigure.getModel().getLength();
		horizontal = arrayFigure instanceof ArrayPrimitiveFigure;
		vars = ref.getIndexVars();
		for(IArrayIndexModel v : vars)
			v.registerDisplayObserver((a) -> arrayFigure.repaint());

		fixedVars = ref.getFixedIndexes();

		firstPositionBounds = arrayFigure.getPositionBounds(0);
		lastPositionBounds = arrayFigure.getPositionBounds(N - 1);
		firstLabelBounds = arrayFigure.getLabelBounds(0);
		lastLabelBounds = arrayFigure.getLabelBounds(N - 1);
	}


	@Override
	public Insets getInsets(IFigure figure) {
		int outOfBoundsExtra = firstLabelBounds.width + Constants.ARRAY_POSITION_SPACING * 2;
		if(horizontal){
			return new Insets(0, outOfBoundsExtra, EXTRA, outOfBoundsExtra);
		}else{
			return new Insets(outOfBoundsExtra, EXTRA, outOfBoundsExtra, 0);
		}
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
	public void paint(IFigure figure, Graphics g, Insets insets) {
		final int POS = firstPositionBounds.width + Constants.ARRAY_POSITION_SPACING;
		final Dimension POSITION_DIM = new Dimension(firstPositionBounds.width, Constants.POSITION_WIDTH);
		
		drawOutOfBoundsPositions(figure, g, POS);

		final int pWidth = POSITION_DIM.width;
		final int y = ARRAY_POSITION_SPACING + POSITION_DIM.height + EXTRA;

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

			int textWidth = FigureUtilities.getTextWidth(varName, font);
			Point from = usualTranslation(getIndexLocation(i)).getTranslated(textWidth/2, 0);
			
			g.drawText(varName, from);
			drawHistory(g, v, POS);
			
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
					Point to = usualTranslation(getIndexLocation(boundVal));
					
					drawArrow(g, arrowFrom, getPointDependingOnOrientation(arrowFrom, to));
				}
				
				if(directionOK)
					drawBar(g, boundVal, v.getBound(), v.getDirection(), POS);
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

	private void drawHistory(Graphics g, IArrayIndexModel v, int POS) {
		g.setBackgroundColor(ColorConstants.gray);
		List<String> history = v.getHistory();
		for (int j = 0; j < history.size()-1; j++) {
			Integer i = Integer.parseInt(history.get(j));
			Point p = usualTranslation(getIndexLocation(i));
			
			if(horizontal){
				p = p.getTranslated(0, EXTRA);
			}else{
				p = p.getTranslated(0, firstPositionBounds.height/2);
			}
			g.fillOval(p.x, p.y, 4, 4);
		}
	}


	private void setIllustrationStyle(Graphics g) {
		g.setLineWidth(Constants.ILLUSTRATION_LINE_WIDTH);
		g.setLineDashOffset(2.5f);
		g.setLineStyle(Graphics.LINE_SOLID);
		g.setForegroundColor(Constants.Colors.ILLUSTRATION);
	}


	private Point getIndexLocation(int index) {
		Point p = null;
		final int distance = firstPositionBounds.width + Constants.ARRAY_POSITION_SPACING;
		if(index < 0)
			p = arrayFigure.getPositionBounds(0).getLocation()
					.getTranslated(horizontal ? -distance : 0, horizontal ? 0 : -distance);
		else if(index >= N)
			p = arrayFigure.getPositionBounds(N-1).getLocation()
					.getTranslated(horizontal ? distance : 0,  horizontal ? 0 : distance);
		else
			p = arrayFigure.getPositionBounds(index).getLocation();
		return p;
	}

	private void drawOutOfBoundsPositions(IFigure figure, Graphics g, int POS) {
		final Dimension rectDim = firstLabelBounds.getSize();
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

		if(exception == ExceptionType.ARRAY_INDEX_OUT_BOUNDS)
			g.setForegroundColor(Constants.Colors.ERROR);
		
		if(showLeft) {
			Point origin = firstLabelBounds.getLocation();
			Point p;
			if(horizontal){
				p = new Point(origin.x - POS - Constants.ARRAY_POSITION_SPACING, origin.y);
			}else{
				p = new Point(origin.x, origin.y - POS/2 - Constants.ARRAY_POSITION_SPACING);
			}
			
			g.setLineDashOffset(2.5f);
			g.setLineStyle(Graphics.LINE_DASH);
			g.drawRectangle(new Rectangle(p, rectDim));
			g.setForegroundColor(Constants.Colors.ILLUSTRATION);
		}
		
		if(showRight) {
			Point origin = lastLabelBounds.getLocation();
			
			Point p;
			if(horizontal){
				p = new Point(origin.x + POS  + Constants.ARRAY_POSITION_SPACING, origin.y);
			}else{
				p = new Point(origin.x, origin.y + POS - Constants.ARRAY_POSITION_SPACING*3);
			}
			
			g.setLineDashOffset(2.5f);
			g.setLineStyle(Graphics.LINE_DASH);
			g.drawRectangle(new Rectangle(p, rectDim));
		}
		
	}

	private void drawArrow(Graphics g, Point from, Point to) {
		g.setLineStyle(Graphics.LINE_SOLID);
		if(horizontal){
			int xx = from.x < to.x ? -ARROW_EDGE : ARROW_EDGE;
			g.drawLine(to, to.getTranslated(xx, -ARROW_EDGE));
			g.drawLine(to, to.getTranslated(xx, ARROW_EDGE));
		}else{
			to = to.translate(0, from.y < to.y ? firstLabelBounds.height : -firstLabelBounds.height);
			int xx = from.y < to.y ? -ARROW_EDGE : ARROW_EDGE;
			g.drawLine(to, to.getTranslated(-ARROW_EDGE, xx));
			g.drawLine(to, to.getTranslated(ARROW_EDGE, xx));
		}
		g.drawLine(from, to);
	}

	private void drawBar(Graphics g, Integer boundVal, IBound bound, Direction direction, int POS) {
		if(direction != Direction.NONE) {
			Point origin = usualTranslation(getIndexLocation(boundVal));
			
			int w;
			boolean open = direction == Direction.FORWARD && bound.getType() == BoundType.CLOSE ||
					direction == Direction.BACKWARD && bound.getType() == BoundType.OPEN;
			if(horizontal){
				w = firstLabelBounds.width/2 + Constants.ARRAY_POSITION_SPACING; 
				if(!open)
					w= -w;
			}else{ 
				if(!open)
					w = -Constants.ARRAY_POSITION_SPACING * 2;
				else
					w = firstPositionBounds.height + Constants.ARRAY_POSITION_SPACING;
			}

			origin.translate(horizontal ? w : 0, horizontal ? 0 : w);

			Point from = origin.getTranslated(horizontal ? 0 : -BAR_HEIGHT, horizontal ? -BAR_HEIGHT : 0);; 

			Point to = origin.getTranslated(horizontal ? 0 : BAR_HEIGHT, horizontal ? BAR_HEIGHT : 0);
			setIllustrationStyle(g);
			g.drawLine(from, to);
		}
	}


	private Point usualTranslation(Point origin) {
		if(horizontal){
			origin.translate(firstPositionBounds.width/2, firstPositionBounds.height + Constants.ARRAY_POSITION_SPACING);
		}else{
			origin.translate(-firstPositionBounds.height, -Constants.ARRAY_POSITION_SPACING);
		}
		return origin;
	}
}