package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.Constants.ARROW_EDGE;
import static pt.iscte.pandionj.Constants.ARROW_LINE_WIDTH;
import static pt.iscte.pandionj.Constants.INDEX_FONT_SIZE;
import static pt.iscte.pandionj.Constants.OBJECT_CORNER;
import static pt.iscte.pandionj.Constants.POSITION_WIDTH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.tests.Observable2;

public class ArrayPrimitiveFigure extends PandionJFigure {
	private static final GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
	private final IArrayModel<IValueModel> model; // array being displayed
	private final int N; // array length
	private List<Position> positions; // existing array positions
	private Map<String, IArrayIndexModel> vars; // variables (roles) associated with the array

	private GridLayout outerLayout;
	private GridLayout arrayLayout;
	private RoundedRectangle positionsFig;
	private PositionOutBounds leftBound; 
	private PositionOutBounds rightBound;

	public ArrayPrimitiveFigure(IArrayModel<IValueModel> model) {
		super(model);
		this.model = model;
		model.registerDisplayObserver((o, indexes) -> observerAction(o, indexes));
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT); // limit size
		positions = new ArrayList<>(N);

		setBackgroundColor(Constants.Colors.OBJECT);

		outerLayout = new GridLayout(3, false);
		setLayoutManager(outerLayout);

		leftBound = new PositionOutBounds();
		rightBound = new PositionOutBounds();
		positionsFig = createPositionsFig();
		
		GridData boundConstraints = new GridData(SWT.CENTER, SWT.TOP, false, false);
		add(leftBound, boundConstraints);
		add(positionsFig);
		add(rightBound, boundConstraints);
		
		vars = new HashMap<>();
//		for(IArrayIndexModel v : model.getIndexModels())
//			addVariable(v);

		updateOutOfBoundsPositions();
		setSize(getPreferredSize());
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) { 
		int varSpace = vars.size() * 30;
		return super.getPreferredSize(wHint, hHint).expand(0, varSpace);
	}


	private RoundedRectangle createPositionsFig() {
		RoundedRectangle fig = new RoundedRectangle();
		arrayLayout = new GridLayout(Math.max(1, N), false);
		arrayLayout.horizontalSpacing = ARRAY_POSITION_SPACING;
		arrayLayout.marginHeight = ARRAY_POSITION_SPACING*2;
		fig.setLayoutManager(arrayLayout);
		fig.setCornerDimensions(OBJECT_CORNER);
		
		Label lengthLabel = new Label("length = " + N);
		fig.setToolTip(lengthLabel);
		if(N == 0) {
			fig.setPreferredSize(new Dimension(Constants.POSITION_WIDTH/2,Constants.POSITION_WIDTH));
		}
		else if(model.getLength() <= Constants.ARRAY_LENGTH_LIMIT) {
			for(int i = 0; i < N; i++) {
				Position p = new Position(i);
				fig.add(p);
				positions.add(p);
			}
		}else {
			for(int i = 0; i < Constants.ARRAY_LENGTH_LIMIT - 2; i++) {
				Position p = new Position(i);
				fig.add(p);
				positions.add(p);
			}
			Position emptyPosition = new Position(null);
			fig.add(emptyPosition);
			Position lastPosition = new Position(model.getLength() - 1);
			fig.add(lastPosition);
			positions.add(lastPosition);
		}
		return fig;
	}


	private class Position extends Figure {
		private ValueLabel valueLabel;
		private Label indexLabel;

		public Position(Integer index) {
			int width = POSITION_WIDTH;
			if(model.isDecimal())
				width *= 2;

			GridData layoutData = new GridData(width, POSITION_WIDTH+20);
			arrayLayout.setConstraint(this, layoutData);
			GridLayout layout = Constants.getOneColGridLayout();
			setLayoutManager(layout);

			if(index != null) {
				IValueModel m = model.getElementModel(index); 
				valueLabel = new ValueLabel(m);
				layout.setConstraint(valueLabel, new GridData(width, POSITION_WIDTH));
				add(valueLabel);
			}else {
				Label emptyLabel = new Label("...");
				FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
				IValueModel measure = model.getElementModel(model.getLength() - 1);
				setSize(measure.isDecimal() || measure.isBoolean() ? Constants.POSITION_WIDTH*2 : Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);
				layout.setConstraint(emptyLabel, new GridData(width, POSITION_WIDTH));
				add(emptyLabel);
			}

			indexLabel = new Label(index == null ? "..." : Integer.toString(index));
			FontManager.setFont(indexLabel, INDEX_FONT_SIZE);
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);
		}
	}

	private class PositionOutBounds extends Figure {
		public static final int TOP_PADDING = ARRAY_POSITION_SPACING*2;
		private boolean error;

		public PositionOutBounds() {
			error = false;
			setVisible(false);
			setSize(POSITION_WIDTH, POSITION_WIDTH + TOP_PADDING * 2);
		}

		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			graphics.setForegroundColor(error ? Constants.Colors.ERROR : ColorConstants.gray);
			graphics.setLineWidth(Constants.ARRAY_LINE_WIDTH);
			graphics.setLineDashOffset(2.5f);
			graphics.setLineStyle(Graphics.LINE_DASH);
			graphics.drawRectangle(getLocation().x, getLocation().y + TOP_PADDING, POSITION_WIDTH-1, POSITION_WIDTH-1);
		}

		public void markError() {
			error = true;
			setToolTip(new Label("Illegal access to position"));
			repaint();
		}
	}

	private void addVariable(IArrayIndexModel varModel) {
		vars.put(varModel.getName(), varModel);
		varModel.registerDisplayObserver((o,a) -> {
			updateOutOfBoundsPositions();
			repaint();
		});
	}

	private void updateOutOfBoundsPositions() {
		boolean lowerOff = false;
		boolean upperOff = false;
		for(IArrayIndexModel v : vars.values()) {
			if (v.getCurrentIndex() < 0 || (v.getBound() != null && v.getBound().getValue() < 0))
				lowerOff = true;
			if (v.getCurrentIndex() >= N || (v.getBound() != null && v.getBound().getValue() >= N))
				upperOff = true;
		}
		
		leftBound.setVisible(lowerOff); 
		rightBound.setVisible(upperOff);
	}

	private void observerAction(Observable2 o, Object arg) {
		if(arg instanceof IndexOutOfBoundsException) {
			System.out.println("Index fora");
			updateOutOfBoundsPositions();
			for(IArrayIndexModel v : vars.values())
				if(isOutOfBounds(v.getCurrentIndex()))
					if(v.getCurrentIndex() < 0)
						leftBound.markError();
					else
						rightBound.markError();
		}
		else if(arg instanceof IArrayIndexModel) {
			addVariable((IArrayIndexModel) arg); 
		}
	}


	@Override
	public void paintFigure(final Graphics graphics) {
		super.paintFigure(graphics);

		Dimension dim = N == 0 ? new Dimension(10,10) : positions.get(0).getSize();
		int pWidth = dim.width / 2;
		int y = ARRAY_POSITION_SPACING + dim.height;
		graphics.setLineWidth(ARROW_LINE_WIDTH);
		Font font = FontManager.getFont(Constants.VAR_FONT_SIZE);
		graphics.setFont(font);

		for(IArrayIndexModel v : vars.values()) {
			int i = v.getCurrentIndex();
			String text = v.getName();
			
			Point from;
			if(isOutOfBounds(i)) {
				text += "=" + i;
				from = getIndexLocation(i).getTranslated(pWidth - FigureUtilities.getTextWidth(text, font)/2, y);
			}
			
			if(v.getBound() != null) {
				boolean right = i < v.getBound().getValue();
				from = getIndexLocation(i).getTranslated(pWidth - FigureUtilities.getTextWidth(text, font)/2, y);
				Point to = getIndexLocation(v.getBound().getValue()).getTranslated(pWidth + (right ? -ARROW_EDGE : ARROW_EDGE), y + pWidth);
				graphics.drawText(text, from);
				if(v.getBound().getValue() != i && ((right && i <= model.getLength() - 1) || (!right && i >= 0))) {
					drawArrow(from, to, pWidth, right, graphics);
					
					if(v.getBound().getValue() < 0 || v.getBound().getValue() >= N) {
						text = Integer.toString(v.getBound().getValue());
						to = to.getTranslated(0, -pWidth);
						graphics.drawText(text, right ? to : to.getTranslated(-FigureUtilities.getTextWidth(text, font)/2, 0));
					}
				}
				y += pWidth;
			}else {
				graphics.drawText(v.getName(), getIndexLocation(v.getCurrentIndex()).getTranslated(0, y));
				y += pWidth;
				if(v.getDirection() != Direction.NONE) {
					boolean right = v.getDirection() == Direction.FORWARD;
					Point origin = getIndexLocation(i).getTranslated(0, y + pWidth);
					from = right ?  origin : origin.getTranslated(pWidth, 0);
					Point to = right ? origin.getTranslated(pWidth, 0) : origin;
					drawArrow(from, to, 0, right, graphics);
					y += pWidth;
				}
			}
		}
	}
	
	private void drawArrow(Point from, Point to, int pWidth, boolean right, Graphics graphics) {
		Point arrowTo = to.getTranslated(right ? 0 : pWidth/2, 0);
		graphics.drawLine(from.getTranslated(right ? pWidth : 0, pWidth), arrowTo);
		Point a = arrowTo.getTranslated(right ? -ARROW_EDGE : ARROW_EDGE, -ARROW_EDGE);
		graphics.drawLine(arrowTo, a);
		a = a.getTranslated(0, ARROW_EDGE*2);
		graphics.drawLine(arrowTo, a);
	}
	
	@Override
	protected void paintBorder(Graphics graphics) {
		super.paintBorder(graphics);
		for(IArrayIndexModel v : vars.values()) {
			if(v.getBound() != null) {
				Point origin = getIndexLocation(v.getBound().getValue());
				Point from = origin.getTranslated(0, -100);
				Point to = origin.getTranslated(0, 100);
				graphics.drawLine(from, to);
			}
		}
	}

	private boolean isOutOfBounds(int i) {
		return i < 0 || i >= N;
	}
	
	private Point getIndexLocation(int index) {
		if(index >= N || N == 0) {
			return rightBound.getLocation().getTranslated(0.0, PositionOutBounds.TOP_PADDING);
		}else if(index < 0) {
			return leftBound.getLocation().getTranslated(0.0, PositionOutBounds.TOP_PADDING);
		}else {
			return positions.get(index).getLocation();
		}
	}
}