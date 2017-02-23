package pt.iscte.pandionj.figures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ValueModel;

public class ArrayReferenceFigure extends Figure implements Observer {
//	static final int POSITION_WIDTH_EMPTY = Constants.POSITION_WIDTH/2;

	private static final int INDEX_FONT_SIZE = 14;
	private static final int POSITION_LINE_WIDTH = 1;
	private static final int POSITION_SPACING = 1;
	private static final int VAR_FONT_SIZE = 20;
	private static final Font VAR_FONT = new Font(null, "Arial", VAR_FONT_SIZE, SWT.NONE);

	private static Color[] VARCOLORS = {
			ColorConstants.red, ColorConstants.blue, ColorConstants.green
	};

	enum Direction {
		NONE, FORWARD, BACKWARD;
	}

	private class Var {
		final String id;
		final Color color;
		List<Integer> indexes;
		Object bound;
		Var(String id, int index, Object bound, Color color) {
			assert bound == null || bound instanceof Integer || bound instanceof String && vars.containsKey((String) bound);
			this.id = id;
			this.indexes = new ArrayList<>();
			indexes.add(index);
			this.bound = bound;
			this.color = color;
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
			int i =  getCurrentIndex() ;
			int b = getBound();
			return b == -1 || b == i ? Direction.NONE : i < b ? Direction.FORWARD : Direction.BACKWARD;
		}

		public void updateIndex(int index) {
			indexes.add(index);			
		}

		public List<Integer> getIndexes() {
			return indexes;
		}


	}

	private final int N;
	private int lowerOffSet;
	private List<Position> positions;
	private Map<String, Var> vars;

	private GridLayout layout;
	private ArrayModel model;

	public ArrayReferenceFigure(ArrayModel model) {
		this.model = model;
		N = model.getLength();
		lowerOffSet = 0;

		positions = new ArrayList<>(N+2);
		vars = new HashMap<>();

		GridLayout layout = new GridLayout(1, true);
		setLayoutManager(layout);
	
		GridLayout layout2 = new GridLayout(1, true);
		layout2.horizontalSpacing = POSITION_SPACING;
		
		positionsFig = new Figure();
		positionsFig.setLayoutManager(layout2);
		add(positionsFig);
			
		if(N == 0) {
			positionsFig.add(new Position(null, true));
		}
		else {
			
			for(int i = 0; i < N; i++) {
				Position p = new Position(i, false);
				p.setValue(model.get(i));
				positionsFig.add(p);
				positions.add(p);
			}
		}
		
		Label lengthLabel = new Label("length = " + N);
		layout.setConstraint(lengthLabel, new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		add(lengthLabel);

		
		
		//		setOpaque(true);
		setBackgroundColor(ColorConstants.white);
		setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		setSize(-1,-1);

		model.registerObserver(this);

		for(ValueModel v : model.getVars())
			addVariable(v);
		
		
	}
	
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		return super.getPreferredSize(wHint, hHint);
//		return new Dimension(
//				Constants.MARGIN * 2 + (int) (Constants.POSITION_WIDTH*(N==0 ? 1.5 : 2.0)) + vars.size() * Constants.ARROW_EDGE * 2,
//				Constants.MARGIN * 3 + (positions.size() == 0 ?  Constants.POSITION_WIDTH : POSITION_SPACING + (Constants.POSITION_WIDTH + POSITION_SPACING*2)*positions.size())
//		);
	}

	private void addVariable(ValueModel varModel) {

		setVar(varModel.getName(), Integer.parseInt(varModel.getCurrentValue()), 3, false);
		varModel.registerObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				setVar(varModel.getName(), Integer.parseInt(varModel.getCurrentValue()), 3, false);
				repaint();
			}
		});
	}

	@Override
	public void update(Observable o, Object index) {
		if(index instanceof Integer) {
			Integer i = (Integer) index;
			for(int j = 0; j < N; j++) {
				if(j == i)
					getPosition(i).highlight();
				else
					getPosition(i).unhighlight();
			}
			getPosition(i).setValue(model.get(i));
		}
		else if(index instanceof ValueModel) {
			addVariable((ValueModel) index);
		}
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
		layout.horizontalSpacing = POSITION_SPACING;
		positionsFig.setLayoutManager(layout);
		repaint();
	}

	@Override
	protected void paintFigure(final Graphics graphics) {
		super.paintFigure(graphics);
		Dimension dim = N == 0 ? new Dimension(5, 5) : getPosition(0).getSize();
		int pWidth = dim.width / 2;
		int y = Constants.OBJECT_PADDING + dim.height;
		graphics.setLineWidth(Constants.ARROW_LINE_WIDTH);
		graphics.setFont(VAR_FONT);
		for(Var v : vars.values()) {
			graphics.setForegroundColor(v.color);
			int i = v.getCurrentIndex();
			boolean forward = v.getDirection().equals(Direction.FORWARD);


			Point from = getPosition(i).getLocation().getTranslated(pWidth - FigureUtilities.getTextWidth(v.id, VAR_FONT)/2, y);
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
			v = new Var(id, index, bound, VARCOLORS[vars.size()]);
			vars.put(id, v);
		}
		else {
			v.updateIndex(index);
			v.bound = bound;
		}
		repaint();
	}

	public void addVarBound(int index, String id) {
		vars.put(id, new Var(id, index, -1, VARCOLORS[vars.size()]));
		repaint();
	}

//	public boolean setValue(int index, Object value) {
//		Position p = getPosition(index);
//		boolean change = !p.getValue().equals(value.toString());
//		p.setValue(value.toString());
//		return change;
//	}

	private class Position extends Figure {
		private final Label valueLabel;
		private boolean outOfBounds;
		private int width;

		public Position(Integer index, boolean outOfBounds) {
			this.outOfBounds = outOfBounds;

			width = index != null ? Constants.POSITION_WIDTH : Constants.POSITION_WIDTH;
			GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			GridData layoutData = new GridData(width, Constants.POSITION_WIDTH);
			GridLayout layout = new GridLayout(2, false);
			layout.verticalSpacing = 0;
			layout.horizontalSpacing = 0;
			layout.marginWidth = 0;
			layout.marginHeight = 0;

			Label indexLabel = new Label(indexText(index));
			indexLabel.setFont(new Font(null, "Arial", INDEX_FONT_SIZE, SWT.NONE));
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);
			
			setLayoutManager(layout);
			valueLabel = new Label("");
			valueLabel.setFont(new Font(null, "Arial", Constants.VALUE_FONT_SIZE, SWT.NONE));
			valueLabel.setOpaque(!outOfBounds);
			if(!outOfBounds) {
				LineBorder lineBorder = new LineBorder(ColorConstants.black, POSITION_LINE_WIDTH, outOfBounds ? Graphics.LINE_DASH : Graphics.LINE_SOLID);
				valueLabel.setBorder(lineBorder);
			}
			layout.setConstraint(valueLabel, layoutData);
			add(valueLabel);


		}

		public String getValue() {
			return valueLabel.getText();
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
				graphics.drawRectangle(getLocation().x, getLocation().y, width-1, Constants.POSITION_WIDTH-1);
			}
		}

		public void setValue(String value) {
			valueLabel.setText(value);
		}

		public void highlight() {
//			valueLabel.setBackgroundColor(ColorConstants.cyan);
		}

		public void unhighlight() {
//			valueLabel.setBackgroundColor(ColorConstants.white);
		}
	}

	private Figure positionsFig;
	

	public void highlight(int i) {
		getPosition(i).highlight();
	}

	public void unhighlight(int i) {
		getPosition(i).unhighlight();
	}


}
