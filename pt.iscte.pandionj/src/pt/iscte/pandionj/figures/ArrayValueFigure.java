package pt.iscte.pandionj.figures;

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
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import static pt.iscte.pandionj.Constants.*;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ValueModel;

public class ArrayValueFigure extends Figure {
	private final ArrayModel model;
	private final int N; // array length
	private List<Position> positions; // existing array positions
	private Map<String, Var> vars; // variables associated with the array
	private int lowerOffSet; // number of positions below the lower bound (given by vars)

	private GridLayout layout;
	private Figure positionsFig;

	public ArrayValueFigure(ArrayModel model) {
		this.model = model;
		model.addObserver((o, index) -> observerAction(o, index));
		N = model.getLength();

		positions = new ArrayList<>(N+2);
		lowerOffSet = 0;

		vars = new HashMap<>();
		for(ValueModel v : model.getVars())
			addVariable(v);

		setOpaque(false);
		setLayoutManager(getOneColGridLayout());
		setBorder(new MarginBorder(MARGIN));
		setSize(-1,-1);

		RoundedRectangle fig = new RoundedRectangle();
		fig.setCornerDimensions(OBJECT_CORNER);
		fig.setBackgroundColor(OBJECT_COLOR);
		add(fig);

		layout = getOneColGridLayout();
		fig.setLayoutManager(layout);

		Label lengthLabel = new Label("length = " + N);
		fig.add(lengthLabel);
		layout.setConstraint(lengthLabel, new GridData(SWT.CENTER, SWT.CENTER, true, false));

		positionsFig = createPositionsFig(model);
		fig.add(positionsFig);
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) { // (positions.get(0).getBounds().width + ARRAY_POSITION_SPACING*2)
		int varSpace = vars.size() * 20 * ARROW_EDGE * 2;
		return super.getPreferredSize(wHint, hHint).expand(0, + varSpace);
	}

	
	private Figure createPositionsFig(ArrayModel model) {
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

	private Position getExistingPosition(int arrayIndex) {
		assert arrayIndex >= 0 && arrayIndex < N;
		return positions.get(lowerOffSet + arrayIndex);
	}

	private void addVariable(ValueModel varModel) {

		Display.getDefault().syncExec(() -> setVar(varModel.getName(), Integer.parseInt(varModel.getCurrentValue()), null, false));
		varModel.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Display.getDefault().syncExec(() -> {
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
			for(int j = 0; j < N; j++) {
				if(j == i)
					getExistingPosition(i).highlight();
				else
					getExistingPosition(i).unhighlight();
			}
			getExistingPosition(i).setValue(model.get(i));
		}
		else if(index instanceof ValueModel) {
			addVariable((ValueModel) index);
		}
		else if(index instanceof RuntimeException) {
			String v = ((RuntimeException) index).getMessage();
			int currentIndex = vars.get(v).getCurrentIndex();
			positions.get(lowerOffSet + currentIndex).markError();
		}
	}






	private static final Font VAR_FONT = new Font(null, FONT_FACE, VAR_FONT_SIZE, SWT.NONE);

	@Override
	protected void paintFigure(final Graphics graphics) {
		super.paintFigure(graphics);

		Dimension dim = N == 0 ? new Dimension(5, 5) : positions.get(0).getSize();
		int pWidth = dim.width / 2;
		int y = MARGIN + dim.height;
		graphics.setLineWidth(ARROW_LINE_WIDTH);
		graphics.setFont(VAR_FONT);
		for(Var v : vars.values()) {
			graphics.setForegroundColor(v.color);
			int i = v.getCurrentIndex();
			boolean forward = v.getDirection().equals(Direction.FORWARD);

			if(v.isBar()) {
				Point from = positions.get(i).getLocation().getTranslated(POSITION_WIDTH/2-ARROW_LINE_WIDTH*3, -MARGIN);
				Point to = from.getTranslated(0, y + vars.size()*(ARROW_EDGE*2));
				graphics.drawLine(from, to);
				graphics.drawText(v.id, to);
			}


			Point from = positions.get(i).getLocation().getTranslated(pWidth - FigureUtilities.getTextWidth(v.id, VAR_FONT)/2, y);
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



	public boolean setValue(int index, Object value) {
		Position p = getExistingPosition(index);
		boolean change = !p.getValue().equals(value.toString());
		p.setValue(value.toString());
		return change;
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
			SET_FONT(valueLabel, VALUE_FONT_SIZE);

			if(!outOfBounds) {
				LineBorder lineBorder = new LineBorder(ColorConstants.black, POSITION_LINE_WIDTH, outOfBounds ? Graphics.LINE_DASH : Graphics.LINE_SOLID);
				valueLabel.setBorder(lineBorder);
				valueLabel.setBackgroundColor(ARRAY_POSITION_COLOR);
				valueLabel.setOpaque(true);
			}
			layout.setConstraint(valueLabel, layoutData);
			add(valueLabel);

			indexLabel = new Label(index == null ? "" : Integer.toString(index));
			SET_FONT(indexLabel, INDEX_FONT_SIZE);
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
			Display.getDefault().syncExec(() -> valueLabel.setText(value));
		}

		public void highlight() {
			Display.getDefault().syncExec(() -> valueLabel.setBackgroundColor(Constants.HIGHLIGHT_COLOR));
		}

		public void unhighlight() {
			Display.getDefault().syncExec(() -> valueLabel.setBackgroundColor(Constants.ARRAY_POSITION_COLOR));
		}
		
		public void markError() {
			error = true;
			indexLabel.setForegroundColor(Constants.ERROR_COLOR);
			repaint();
		}
	}


	public void highlight(int i) {
		getExistingPosition(i).highlight();
	}

	public void unhighlight(int i) {
		getExistingPosition(i).unhighlight();
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
