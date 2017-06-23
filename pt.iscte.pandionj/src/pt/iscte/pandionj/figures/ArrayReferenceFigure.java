package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.OBJECT_CORNER;
import static pt.iscte.pandionj.Constants.getOneColGridLayout;
import static pt.iscte.pandionj.Constants.Colors.OBJECT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.figures.Var.Direction;

//TODO limit size (to Constants.ARRAY_LENGTH_LIMIT)
public class ArrayReferenceFigure extends RoundedRectangle {
	private final int N;
	private int lowerOffSet;
	private List<Position> positions;
	private Map<String, Var> vars;

	private GridLayout layout;
	private IArrayModel model;

	private Figure positionsFig;

	
	public ArrayReferenceFigure(IArrayModel model) {
		this.model = model;
		N = model.getLength();
		lowerOffSet = 0;

		positions = new ArrayList<>(N+2);
		vars = new HashMap<>();

		GridLayout layout = new GridLayout(1, true);
		setLayoutManager(layout);
		
		GridLayout layout2 = new GridLayout(1, true);
		layout2.horizontalSpacing = Constants.ARRAY_POSITION_SPACING;
		layout2.marginWidth = 0;
		
		setCornerDimensions(OBJECT_CORNER);
		setBackgroundColor(OBJECT);
		setOpaque(false);
		
	
		layout = getOneColGridLayout();
		setLayoutManager(layout);
		
		positionsFig = new Figure();
		positionsFig.setLayoutManager(layout2);
		positionsFig.setOpaque(false);
		add(positionsFig);
			
		setOpaque(false);
		
		if(N == 0) {
			positionsFig.add(new Position(null, true));
		}
		else {
			for(int i = 0; i < N; i++) {
				Position p = new Position(i, false);
				positionsFig.add(p);
				positions.add(p);
			}
		}
		
		
		Label lengthLabel = new Label("length = " + N);
		setToolTip(lengthLabel);

		
//		for(ReferenceModel r : model.getModelElements())
//			r.registerDisplayObserver(new Observer() {
//				@Override
//				public void update(Observable o, Object arg) {
//					// TODO Auto-generated method stub
//					System.out.println("update");
//				}
//			});
	}
	

	private Position getPosition(int arrayIndex) {
		assert arrayIndex >= 0 && arrayIndex < N;
		return positions.get(lowerOffSet + arrayIndex);
	}



	public void addOutOfUpperBound(int index) {
		for(int i = 0; i < index-(N-1); i++) {
			Position p = new Position(positions.size(), true);
			positionsFig.add(p);
			positions.add(p);
		}
		changeLayout();
	}


	public void addOutOfLowerBound(int index) {
		for(int i = -1; i > index; i--) {
			Position p = new Position(i, true);
			positionsFig.add(p, 0);
			positions.add(p);
		}
		lowerOffSet = lowerOffSet + (lowerOffSet-index);
		changeLayout();
	}
	private void changeLayout() {
		layout = new GridLayout(positions.size(), true);
		layout.horizontalSpacing = Constants.ARRAY_POSITION_SPACING;
		positionsFig.setLayoutManager(layout);
		repaint();
	}

	@Override
	public void paintFigure(final Graphics graphics) {
		super.paintFigure(graphics);
		Dimension dim = N == 0 ? new Dimension(5, 5) : getPosition(0).getSize();
		int pWidth = dim.width / 2;
		int y = Constants.OBJECT_PADDING + dim.height;
		graphics.setLineWidth(Constants.ARROW_LINE_WIDTH);
		Font font = FontManager.getFont(Constants.VAR_FONT_SIZE);
		graphics.setFont(font);
		for(Var v : vars.values()) {
			graphics.setForegroundColor(v.color);
			int i = v.getCurrentIndex();
			boolean forward = v.getDirection().equals(Direction.FORWARD);


			Point from = getPosition(i).getLocation().getTranslated(pWidth - FigureUtilities.getTextWidth(v.id, font)/2, y);
			graphics.drawText(v.id, from);

			List<Integer> indexes = v.getIndexes();
			for(int iOld = 0; iOld < indexes.size()-1; iOld++) {
				Point p = getPosition(indexes.get(iOld)).getLocation().getTranslated(pWidth, y + vars.size()*Constants.ARROW_EDGE);
				graphics.drawOval(p.x-1, p.y-1, 2, 2);
			}

			if(v.isBounded()) {
				if(i != v.getBound()) {
					Point to = positions.get(v.getBound()).getLocation().getTranslated(pWidth + (forward ? -Constants.ARROW_EDGE : Constants.ARROW_EDGE), y+Constants.ARROW_EDGE);
					from = from.getTranslated(forward ? Constants.ARROW_EDGE : -Constants.ARROW_EDGE, Constants.ARROW_EDGE);
					graphics.drawLine(from, to);
					Point a = to.getTranslated(forward ? -Constants.ARROW_EDGE : Constants.ARROW_EDGE, -Constants.ARROW_EDGE);
					graphics.drawLine(to, a);
					a = a.getTranslated(0, Constants.ARROW_EDGE*2);
					graphics.drawLine(to, a);
				}
			}
			y += Constants.ARROW_EDGE*2;
		}

	}

	public void setVar(String id, int index, Object bound, boolean isBar) {
		if(index >= N && positions.size() - 1 -lowerOffSet < index)
			addOutOfUpperBound(index);
		else if(bound instanceof Integer && ((Integer) bound) >= N)
			addOutOfUpperBound((Integer) bound);
		else if(index + lowerOffSet < 0) {
			addOutOfLowerBound(index);
		}

		Var v = vars.get(id);
		if(v == null) {
			v = new Var(id, index, bound, false, Constants.Colors.getVarColor(vars.size()));
			vars.put(id, v);
		}
		else {
			v.updateIndex(index);
			v.bound = bound;
		}
		repaint();
	}

	public void addVarBound(int index, String id) {
		vars.put(id, new Var(id, index, -1, false, Constants.Colors.getVarColor(vars.size())));
		repaint();
	}

//	public boolean setValue(int index, Object value) {
//		Position p = getPosition(index);
//		boolean change = !p.getValue().equals(value.toString());
//		p.setValue(value.toString());
//		return change;
//	}

	public AbstractConnectionAnchor getAnchor(int positionIndex) {
		return positions.get(positionIndex).anchor;
	}
	
	private class Position extends Figure {
		private final Label valueLabel;
		private boolean outOfBounds;
		private Anchor anchor;
		
		public Position(Integer index, boolean outOfBounds) {
			this.outOfBounds = outOfBounds;
			anchor = new Anchor(ArrayReferenceFigure.this);
			
			GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			GridData layoutData = new GridData(Constants.POSITION_WIDTH/2, Constants.POSITION_WIDTH*2);
			GridLayout layout = new GridLayout(1, false);
			layout.verticalSpacing = 5;
			layout.horizontalSpacing = 5;
			layout.marginWidth = 5;
			layout.marginHeight = 5;

			Label indexLabel = new Label(indexText(index));
			FontManager.setFont(indexLabel, Constants.INDEX_FONT_SIZE);
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);
			
			setLayoutManager(layout);
			valueLabel = new Label("");
			valueLabel.setBackgroundColor(Constants.Colors.ARRAY_POSITION);
			valueLabel.setOpaque(true);
			if(!outOfBounds) {
				LineBorder lineBorder = new LineBorder(ColorConstants.black, Constants.POSITION_LINE_WIDTH, Graphics.LINE_SOLID);
				valueLabel.setBorder(lineBorder);
			}
			layout.setConstraint(valueLabel, layoutData);
//			add(valueLabel); // ?


		}

		private String indexText(Integer index) {
			if(index == null) return "";
			else if(index == ArrayReferenceFigure.this.N) return index + " (length)";
			else return Integer.toString(index);
		}
		
		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			if(outOfBounds) {
				graphics.setForegroundColor(ColorConstants.gray);
				graphics.setLineDashOffset(2.5f);
				graphics.setLineStyle(Graphics.LINE_DASH);
				graphics.drawRectangle(getLocation().x, getLocation().y, Constants.POSITION_WIDTH/2, Constants.POSITION_WIDTH-1);
			}
		}
		
		
		
		private class Anchor extends AbstractConnectionAnchor {
			public Anchor(IFigure fig) {
				super(fig);
			}

			@Override
			public Point getLocation(Point reference) {
//				org.eclipse.draw2d.geometry.Rectangle r =  org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
//				r.setBounds(getOwner().getBounds());
//				r.translate(0, 0);
//				r.resize(1, 1);
//				getOwner().translateToAbsolute(r);
				Rectangle r = Position.this.getBounds();
				getOwner().translateToAbsolute(r);
				return Position.this.getLocation().translate(r.width, r.height/2);
			}
		}
	}
	
}
