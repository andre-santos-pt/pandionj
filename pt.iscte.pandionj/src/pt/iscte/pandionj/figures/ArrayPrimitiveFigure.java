package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.ARRAY_POSITION_COLOR;
import static pt.iscte.pandionj.Constants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.Constants.ARROW_EDGE;
import static pt.iscte.pandionj.Constants.ARROW_LINE_WIDTH;
import static pt.iscte.pandionj.Constants.INDEX_FONT_SIZE;
import static pt.iscte.pandionj.Constants.OBJECT_COLOR;
import static pt.iscte.pandionj.Constants.OBJECT_CORNER;
import static pt.iscte.pandionj.Constants.OBJECT_PADDING;
import static pt.iscte.pandionj.Constants.POSITION_LINE_WIDTH;
import static pt.iscte.pandionj.Constants.POSITION_WIDTH;
import static pt.iscte.pandionj.Constants.VALUE_FONT_SIZE;
import static pt.iscte.pandionj.Constants.getOneColGridLayout;
import static pt.iscte.pandionj.Constants.getVarColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.model.ArrayPrimitiveModel;
import pt.iscte.pandionj.model.ValueModel;


// TODO limit size
public class ArrayPrimitiveFigure extends RoundedRectangle {
	private final ArrayPrimitiveModel model;
	private final int N; // array length
	private List<Position> positions; // existing array positions
	private Map<String, Var> vars; // variables associated with the array
	private int lowerOffSet; // number of positions below the lower bound (given by vars)

	private GridLayout layout;
	private Figure positionsFig;

	public ArrayPrimitiveFigure(ArrayPrimitiveModel model) {
		this.model = model;
		model.registerObserver((o, index) -> observerAction(o, index));
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT);
		positions = new ArrayList<>(N+2);
		lowerOffSet = 0;

		setCornerDimensions(OBJECT_CORNER);
		setBackgroundColor(OBJECT_COLOR);
		
		layout = getOneColGridLayout();
		setLayoutManager(layout);

		Label lengthLabel = new Label("length = " + N);
		setToolTip(lengthLabel);

		positionsFig = createPositionsFig(model);
		add(positionsFig);

		vars = new HashMap<>();
		for(ValueModel v : model.getVars())
			addVariable(v);
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) { // (positions.get(0).getBounds().width + ARRAY_POSITION_SPACING*2)
		int varSpace = vars.size() * 20 * ARROW_EDGE * 2;
		return super.getPreferredSize(wHint, hHint).expand(0, + varSpace);
	}


	private Figure createPositionsFig(ArrayPrimitiveModel model) {
		Figure fig = new Figure();
		GridLayout layout = new GridLayout(Math.max(1, N), true);
		layout.horizontalSpacing = ARRAY_POSITION_SPACING;
		fig.setLayoutManager(layout);

		if(N == 0) {
			Position p = new Position(null, true);
			fig.add(p);
			positions.add(p);
			lowerOffSet = 1;
		}
		else {
			for(int i = 0; i < N; i++) {
				Position p = new Position(i, false);
				p.setValue(model.get(i));
				fig.add(p);
				positions.add(p);
			}
			if(N > Constants.ARRAY_LENGTH_LIMIT)
				fig.add(new Label("..."));
		}
		return fig;
	}

	public void ensureOutOfBounds(int index) {
		if(index >= N && index > positions.size() - 1 - lowerOffSet) {
			for(int i = 0; i < index-(N-1); i++) {
				Position p = new Position(positions.size(), true);
				positionsFig.add(p);
				positions.add(p);
			}
			changeLayout();
		}
		else if(index + lowerOffSet < 0) {
			for(int i = -1; i > index; i--) {
				Position p = new Position(i, true);
				positionsFig.add(p, 0);
				positions.add(p);
			}
			lowerOffSet = lowerOffSet + (lowerOffSet-index);
			changeLayout();
		}
	}

	private void changeLayout() {
		layout = new GridLayout(positions.size(), true);
		layout.horizontalSpacing = ARRAY_POSITION_SPACING;
		positionsFig.setLayoutManager(layout);
		repaint();
	}

	private Position getValidPosition(int arrayIndex) {
		assert arrayIndex >= 0 && arrayIndex < N;
		return positions.get(lowerOffSet + arrayIndex);
	}

	private void addVariable(ValueModel varModel) {

		Display.getDefault().asyncExec(() -> setVar(varModel.getName(), Integer.parseInt(varModel.getCurrentValue()), null, false));
		varModel.registerObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Display.getDefault().asyncExec(() -> {
					setVar(varModel.getName(), Integer.parseInt(varModel.getCurrentValue()), null, false);
					repaint();}
						);
			}
		});
	}

	private void setVar(String id, int index, Object bound, boolean isBar) {
		ensureOutOfBounds(index);

		if(bound instanceof Integer) {
			int boundI = (Integer) bound;
			ensureOutOfBounds(boundI);
		}

		Var v = vars.get(id);
		if(v == null) {
			v = new Var(id, index, bound, isBar, getVarColor(vars.size()));
			vars.put(id, v);
		}
		else {
			v.updateIndex(index);
			v.bound = bound;
			v.isBar = isBar;
		}
		repaint();
	}

	private void observerAction(Observable o, Object index) {
		if(index instanceof Integer) {
			Integer i = (Integer) index;
			if(i >= Constants.ARRAY_LENGTH_LIMIT)
				return;
			
			for(int j = 0; j < N; j++) {
				Position p = getValidPosition(j);
				if(j == i)
					p.highlight();
				else
					p.unhighlight();
			}
			getValidPosition(i).setValue(model.get(i));
		}
		else if(index instanceof ValueModel) {
			addVariable((ValueModel) index);
		}
		else if(index instanceof RuntimeException) {
			String v = ((RuntimeException) index).getMessage();
			if(vars.containsKey(v)) {
				int currentIndex = vars.get(v).getCurrentIndex();
				positions.get(lowerOffSet + currentIndex).markError(); // TODO check if out of bounds
			}
		}
	}







	@Override
	public void paintFigure(final Graphics graphics) {
		super.paintFigure(graphics);

		Dimension dim = N == 0 ? new Dimension(5, 5) : positions.get(0).getSize();
		int pWidth = dim.width / 2;
		int y = OBJECT_PADDING + dim.height;
		graphics.setLineWidth(ARROW_LINE_WIDTH);
		Font font = FontManager.getFont(Constants.VAR_FONT_SIZE);

		graphics.setFont(font);
		for(Var v : vars.values()) {
			graphics.setForegroundColor(v.color);
			int i = v.getCurrentIndex();
			boolean forward = v.getDirection().equals(Direction.FORWARD);

			if(v.isBar()) {
				Point from = positions.get(i).getLocation().getTranslated(POSITION_WIDTH/2-ARROW_LINE_WIDTH*3, -OBJECT_PADDING);
				Point to = from.getTranslated(0, y + vars.size()*(ARROW_EDGE*2));
				graphics.drawLine(from, to);
				graphics.drawText(v.id, to);
			}


			Point from = positions.get(i).getLocation().getTranslated(pWidth - FigureUtilities.getTextWidth(v.id, font)/2, y);
			if(!v.isBar()) {
				graphics.drawText(v.id, from);
				//				Dimension box = TextUtilities.INSTANCE.getTextExtents(v.id, graphics.getFont()).expand(10, 10);
				//				if(v.markError) {
				//					graphics.setForegroundColor(ColorConstants.red);
				//					graphics.drawOval(from.x-5, from.y-5, box.width, box.height);
				//				}
			}

			List<Integer> indexes = v.getIndexes();
			for(int iOld = 0; iOld < indexes.size()-1; iOld++) {
				Point p = positions.get(indexes.get(iOld)).getLocation().getTranslated(pWidth, y + vars.size()*ARROW_EDGE);
				graphics.drawOval(p.x-1, p.y+5, 2, 2);
			}

			if(v.isBounded()) {
				if(i != v.getBound()) {
					Point to = positions.get(v.getBound()).getLocation().getTranslated(pWidth + (forward ? -ARROW_EDGE : ARROW_EDGE), y+ARROW_EDGE);
					from = from.getTranslated(forward ? ARROW_EDGE : -ARROW_EDGE, ARROW_EDGE);
					graphics.drawLine(from, to);
					Point a = to.getTranslated(forward ? -ARROW_EDGE : ARROW_EDGE, -ARROW_EDGE);
					graphics.drawLine(to, a);
					a = a.getTranslated(0, ARROW_EDGE*2);
					graphics.drawLine(to, a);
				}


			}
			y += ARROW_EDGE*2;
		}

	}


	private static class Position extends Figure {
		private final Label valueLabel;
		private boolean outOfBounds;
		private int width;
		private boolean error;
		private Label indexLabel;

		public Position(Integer index, boolean outOfBounds) {
			this.outOfBounds = outOfBounds;

			width = index != null ? POSITION_WIDTH : POSITION_WIDTH;
			GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			GridData layoutData = new GridData(width, POSITION_WIDTH);
			GridLayout layout = Constants.getOneColGridLayout();
			setLayoutManager(layout);

			valueLabel = new Label("");
			FontManager.setFont(valueLabel, VALUE_FONT_SIZE);

			if(!outOfBounds) {
				LineBorder lineBorder = new LineBorder(ColorConstants.black, POSITION_LINE_WIDTH, outOfBounds ? Graphics.LINE_DASH : Graphics.LINE_SOLID);
				valueLabel.setBorder(lineBorder);
				valueLabel.setBackgroundColor(ARRAY_POSITION_COLOR);
				valueLabel.setOpaque(true);
			}
			layout.setConstraint(valueLabel, layoutData);
			add(valueLabel);

			indexLabel = new Label(index == null ? "" : Integer.toString(index));
			FontManager.setFont(indexLabel, INDEX_FONT_SIZE);
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);
			error = false;
		}

		public String getValue() {
			return valueLabel.getText();
		}

		//		private String indexText(Integer index) {
		//			if(index == null) return "";
		//			else if(index == ArrayValueFigure.this.N) return index + " (length)";
		//			else return Integer.toString(index);
		//		}

		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			if(outOfBounds) {
				graphics.setForegroundColor(error ? Constants.ERROR_COLOR : ColorConstants.gray);
				graphics.setLineWidth(error ? Constants.ARRAY_LINE_WIDTH*2 : Constants.ARRAY_LINE_WIDTH);
				graphics.setLineDashOffset(2.5f);
				graphics.setLineStyle(Graphics.LINE_DASH);
				graphics.drawRectangle(getLocation().x, getLocation().y, width-1, POSITION_WIDTH-1);
			}
		}

		public void setValue(String value) {
			Display.getDefault().asyncExec(() -> valueLabel.setText(value));
		}

		public void highlight() {
			Display.getDefault().asyncExec(() -> valueLabel.setBackgroundColor(Constants.HIGHLIGHT_COLOR));
		}

		public void unhighlight() {
			Display.getDefault().asyncExec(() -> valueLabel.setBackgroundColor(Constants.ARRAY_POSITION_COLOR));
		}

		public void markError() {
			error = true;
			Display.getDefault().asyncExec(() -> {
				indexLabel.setForegroundColor(Constants.ERROR_COLOR);
				setToolTip(new Label("Illegal access to position " + indexLabel.getText()));
				repaint();
			});
		}
	}


	public void highlight(int i) {
		Position p = getValidPosition(i);
		if(p != null)
			p.highlight();
	}

	public void unhighlight(int i) {
		Position p = getValidPosition(i);
		if(p != null)
			p.unhighlight();
	}


	private enum Direction {
		NONE, FORWARD, BACKWARD;
	}

	private class Var {
		final String id;
		final Color color;
		List<Integer> indexes;
		Object bound;
		boolean isBar;
		boolean markError;

		Var(String id, int index, Object bound, boolean isBar, Color color) {
			assert bound == null || bound instanceof Integer || bound instanceof String && vars.containsKey((String) bound);
			this.id = id;
			this.indexes = new ArrayList<>();
			indexes.add(index);
			this.bound = bound;
			this.color = color;
			this.isBar = isBar;
			markError = false;
		}

		int getCurrentIndex() {
			return indexes.get(indexes.size()-1);
		}

		boolean isBounded() {
			return bound != null;
		}

		int getBound() {
			if(bound == null || bound instanceof String && !vars.containsKey((String) bound))
				return -1;
			else
				return bound instanceof Integer ? (Integer) bound : vars.get((String) bound).getCurrentIndex();
		}

		Direction getDirection() {
			int i = getCurrentIndex() ;
			int b = getBound();
			return b == -1 || b == i ? Direction.NONE : i < b ? Direction.FORWARD : Direction.BACKWARD;
		}

		boolean isBar() {
			return isBar;
		}

		void updateIndex(int index) {
			indexes.add(index);			
		}

		List<Integer> getIndexes() {
			return indexes;
		}

		void markError() {
			markError = true;
		}
	}

}
