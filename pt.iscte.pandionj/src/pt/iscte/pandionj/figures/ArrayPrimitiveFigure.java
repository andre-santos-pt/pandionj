package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.Constants.ARROW_EDGE;
import static pt.iscte.pandionj.Constants.ARROW_LINE_WIDTH;
import static pt.iscte.pandionj.Constants.INDEX_FONT_SIZE;
import static pt.iscte.pandionj.Constants.OBJECT_CORNER;
import static pt.iscte.pandionj.Constants.OBJECT_PADDING;
import static pt.iscte.pandionj.Constants.POSITION_WIDTH;
import static pt.iscte.pandionj.Constants.getOneColGridLayout;
import static pt.iscte.pandionj.Constants.Colors.OBJECT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.ArrayIndexVariableModel;


public class ArrayPrimitiveFigure extends Figure{
	private static final GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
	private final IArrayModel model; // array being displayed
	private final int N; // array length
	private List<Position> positions; // existing array positions
	private Map<String, IArrayIndexModel> vars; // variables (roles) associated with the array

	private GridLayout outerLayout;
	private GridLayout arrayLayout;
	private RoundedRectangle positionsFig;
	private PositionOutBounds leftBound; 
	private PositionOutBounds rightBound;

	public ArrayPrimitiveFigure(IArrayModel model) {
		this.model = model;
		model.registerObserver((o, indexes) -> observerAction(o, indexes));
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT); // limit size
		positions = new ArrayList<>(N);

		setBackgroundColor(Constants.Colors.OBJECT);

		outerLayout = new GridLayout(3, false);
		setLayoutManager(outerLayout);

		Label lengthLabel = new Label("length = " + N);
		setToolTip(lengthLabel);

		leftBound = new PositionOutBounds();
		rightBound = new PositionOutBounds();
		positionsFig = createPositionsFig();
		
		add(leftBound);
		add(positionsFig);
		add(rightBound);
		
		vars = new HashMap<>();
		for(IArrayIndexModel v : model.getIndexModels())
			addVariable(v);

		updateOutOfBoundsPositions();
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) { 
		int varSpace = vars.size() * 20;
		return super.getPreferredSize(wHint, hHint).expand(0, varSpace);
	}


	private RoundedRectangle createPositionsFig() {
		RoundedRectangle fig = new RoundedRectangle() {
			@Override
			public void paintFigure(Graphics graphics) {
				super.paintFigure(graphics);
				graphics.setForegroundColor(ColorConstants.red);
				graphics.drawRectangle(getLocation().x, getLocation().y, 20, 40);
			}
		};
		arrayLayout = new GridLayout(Math.max(1, N), false);
		arrayLayout.horizontalSpacing = ARRAY_POSITION_SPACING;
		fig.setLayoutManager(arrayLayout);
		fig.setCornerDimensions(OBJECT_CORNER);

		
		for(int i = 0; i < N; i++) {
			Position p = new Position(i);
			fig.add(p);
			positions.add(p);
		}
		if(N > Constants.ARRAY_LENGTH_LIMIT) // TODO review
			fig.add(new Label("..."));
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

			IVariableModel m = model.getElementModel(index); 
			valueLabel = new ValueLabel(m);
			layout.setConstraint(valueLabel, new GridData(width, POSITION_WIDTH));
			add(valueLabel);

			indexLabel = new Label(index == null ? "" : Integer.toString(index));
			FontManager.setFont(indexLabel, INDEX_FONT_SIZE);
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);
		}
	}

	private class PositionOutBounds extends Figure {
		private boolean error;

		public PositionOutBounds() {
			error = false;
			setVisible(false);
			setSize(POSITION_WIDTH, POSITION_WIDTH);
		}

		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			graphics.setForegroundColor(error ? Constants.Colors.ERROR : ColorConstants.gray);
			graphics.setLineWidth(error ? Constants.ARRAY_LINE_WIDTH*3 : Constants.ARRAY_LINE_WIDTH);
			graphics.setLineDashOffset(2.5f);
			graphics.setLineStyle(Graphics.LINE_DASH);
			graphics.drawRectangle(getLocation().x, getLocation().y, POSITION_WIDTH-1, POSITION_WIDTH-1);
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
			if (v.getCurrentIndex() < 0)
				lowerOff = true;
			else if (v.getCurrentIndex() >= N)
				upperOff = true;
		}
		leftBound.setVisible(lowerOff); 
		rightBound.setVisible(upperOff);
	}

	// TODO
	private void removeVariable(IVariableModel varModel) {
		vars.remove(varModel.getName());
		PandionJUI.executeUpdate(() -> repaint());
	}


	private void observerAction(Observable o, Object arg) {
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
	}


	@Override
	public void paintFigure(final Graphics graphics) {
		super.paintFigure(graphics);

		Dimension dim = N == 0 ? new Dimension(5, 5) : positions.get(0).getSize();
		int pWidth = dim.width / 2;
		int y = ARRAY_POSITION_SPACING + dim.height;
		graphics.setLineWidth(ARROW_LINE_WIDTH);
		Font font = FontManager.getFont(Constants.VAR_FONT_SIZE);
		graphics.setFont(font);

		for(IArrayIndexModel v : vars.values()) {
			if(!v.isBounded())
				continue;

			int i = v.getCurrentIndex();
			boolean right = i < v.getBound();

			String text = v.getName();
			Point from;
			if(isOutOfBounds(i)) {
				text += "=" + i;
				from = getIndexLocation(i).getTranslated(pWidth - FigureUtilities.getTextWidth(text, font)/2, y);
				graphics.drawText(text, from);
			}else if(v.isBounded()) {
				from = getIndexLocation(i).getTranslated(pWidth - FigureUtilities.getTextWidth(text, font)/2, y);
				Point to = getIndexLocation(v.getBound()).getTranslated(pWidth + (right ? -ARROW_EDGE : ARROW_EDGE), y + pWidth);
				
				if(v.getBound() != i) {
					Point zero = new Point(0, 0);
					Point top = zero.getTranslated(getIndexLocation(i).x, 0);
					graphics.drawLine(top, top.getTranslated(0, y+100));
					
					graphics.drawLine(from.getTranslated(right ? ARROW_EDGE : -ARROW_EDGE, pWidth), to);
					Point a = to.getTranslated(right ? -ARROW_EDGE : ARROW_EDGE, -ARROW_EDGE);
					graphics.drawLine(to, a);
					a = a.getTranslated(0, ARROW_EDGE*2);
					graphics.drawLine(to, a);
				}
				
				graphics.setForegroundColor(ColorConstants.black); // XXX qual a formatacao correta da font?
				to = to.getTranslated(0, -pWidth);
				graphics.drawText(text, from);

				if(v.getBound() < 0 || v.getBound() >= N) {
					graphics.drawText(Integer.toString(v.getBound()), to);
				}
			}
			y += pWidth;
		}
	}

	private boolean isOutOfBounds(int i) {
		return i < 0 || i >= N;
	}
	
	private Point getIndexLocation(int index) {
		if(index >= N || N == 0) {
			return rightBound.getLocation();
		}else if(index < 0) {
			return leftBound.getLocation();
		}else {
			return positions.get(index).getLocation();
		}
	}
}