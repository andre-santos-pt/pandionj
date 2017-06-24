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
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.ArrayIndexVariableModel;


public class ArrayPrimitiveFigure extends Figure {
	private final IArrayModel model; // array being displayed
	private final int N; // array length
	private List<Position> positions; // existing array positions
	private Map<String, ArrayIndexVariableModel> vars; // variables (roles) associated with the array
	private int lowerOffSet; // number of positions below the lower bound (given by vars)

	private GridLayout outerLayout;
	private GridLayout arrayLayout;
	private Figure positionsFig;

	public ArrayPrimitiveFigure(IArrayModel model) {
		this.model = model;
		model.registerObserver((o, indexes) -> observerAction(o, indexes));
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT); // limit size
		positions = new ArrayList<>(N+2);
		lowerOffSet = 0;

//		setCornerDimensions(OBJECT_CORNER);
		setBackgroundColor(ColorConstants.white);

		outerLayout = getOneColGridLayout();
		setLayoutManager(outerLayout);

		Label lengthLabel = new Label("length = " + N);
		setToolTip(lengthLabel);

		positionsFig = createPositionsFig();
		add(positionsFig);

		vars = new HashMap<>();
		for(IVariableModel v : model.getVars())
			addVariable(v);

		updateOutOfBoundsPositions();
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) { 
		int varSpace = vars.size() * 20;
		return super.getPreferredSize(wHint, hHint).expand(0, varSpace);
	}


	private Figure createPositionsFig() {
		Figure fig = new Figure();
		arrayLayout = new GridLayout(Math.max(1, N+2), false);
		arrayLayout.horizontalSpacing = ARRAY_POSITION_SPACING;
		fig.setLayoutManager(arrayLayout);

		if(N == 0) {
			Position p = new Position(-1);
			fig.add(p);
			positions.add(p);
			lowerOffSet = 1;
		}
		else {
			for(int i = -1; i <= N; i++) {
				Position p = new Position(i);
				fig.add(p);
				positions.add(p);
			}
			if(N > Constants.ARRAY_LENGTH_LIMIT) // TODO review
				fig.add(new Label("..."));
		}
		return fig;
	}


	private static final GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);

	private class Position extends Figure {
		private ValueLabel valueLabel;
		private boolean error;
		private Label indexLabel;
		private final boolean outOfBounds;

		public Position(Integer index) {
			outOfBounds = index < 0 || index >= N;

			int width = POSITION_WIDTH;
			if(model.isDecimal())
				width *= 2;

			GridData layoutData = new GridData(width, POSITION_WIDTH+20);
			arrayLayout.setConstraint(this, layoutData);
			GridLayout layout = Constants.getOneColGridLayout();
			setLayoutManager(layout);

			if(!outOfBounds) {
				IVariableModel m = model.getElementModel(index); 
				valueLabel = new ValueLabel(m);
				layout.setConstraint(valueLabel, new GridData(width, POSITION_WIDTH));
				add(valueLabel);
			}
			else
				setVisible(false);

			indexLabel = new Label(index == null ? "" : Integer.toString(index));
			FontManager.setFont(indexLabel, INDEX_FONT_SIZE);
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);
			error = false;
		}

		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			if(outOfBounds) {
				graphics.setForegroundColor(error ? Constants.Colors.ERROR : ColorConstants.gray);
				graphics.setLineWidth(error ? Constants.ARRAY_LINE_WIDTH*3 : Constants.ARRAY_LINE_WIDTH);
				graphics.setLineDashOffset(2.5f);
				graphics.setLineStyle(Graphics.LINE_DASH);
				graphics.drawRectangle(getLocation().x, getLocation().y, POSITION_WIDTH-1, POSITION_WIDTH-1);
			}
		}

		public void markError() {
			error = true;
			indexLabel.setForegroundColor(Constants.Colors.ERROR);
			setToolTip(new Label("Illegal access to position " + indexLabel.getText()));
			repaint();
		}
	}

	private void addVariable(IVariableModel varModel) {
		vars.put(varModel.getName(), new ArrayIndexVariableModel(varModel, N));
		varModel.registerDisplayObserver((o,a) -> {
			updateOutOfBoundsPositions();
			repaint();
		});
	}

	private void updateOutOfBoundsPositions() {
		boolean lowerOff = false;
		boolean upperOff = false;

		for(ArrayIndexVariableModel v : vars.values()) {
			if(v.getCurrentIndex() < 0)
				lowerOff = true;
			//				arrayLayout.setConstraint(positions.get(0), new GridData(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH));
			else if(v.getCurrentIndex() >= N)
				upperOff = true;
			//				arrayLayout.setConstraint(positions.get(N+1), new GridData(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH));
		}
		positions.get(0).setVisible(lowerOff);
		positions.get(N+1).setVisible(upperOff);
	}

	// TODO
	private void removeVariable(IVariableModel varModel) {
		vars.remove(varModel.getName());
		PandionJUI.executeUpdate(() -> repaint());
	}




	private void observerAction(Observable o, Object arg) {
		if(arg instanceof IndexOutOfBoundsException) {
			updateOutOfBoundsPositions();
			for(ArrayIndexVariableModel v : vars.values())
				if(v.isOutOfBounds())
					if(v.getCurrentIndex() < 0)
						positions.get(0).markError();
					else
						positions.get(N+1).markError();
		}
	}


	@Override
	public void paintFigure(final Graphics graphics) {
		super.paintFigure(graphics);

		Dimension dim = N == 0 ? new Dimension(5, 5) : positions.get(1).getSize();
		int pWidth = dim.width / 2;
		int y = ARRAY_POSITION_SPACING + dim.height;
		graphics.setLineWidth(ARROW_LINE_WIDTH);
		Font font = FontManager.getFont(Constants.VAR_FONT_SIZE);
		graphics.setFont(font);

		for(ArrayIndexVariableModel v : vars.values()) {
			//			graphics.setForegroundColor(v.color);
			int i = v.getCurrentIndex();
			boolean forward = v.getDirection().equals(ArrayIndexVariableModel.Direction.FORWARD);

			//			if(v.isBar()) {
			//				Point from = positions.get(i).getLocation().getTranslated(POSITION_WIDTH/2-ARROW_LINE_WIDTH*3, -OBJECT_PADDING);
			//				Point to = from.getTranslated(0, y + vars.size()*(ARROW_EDGE*2));
			//				graphics.drawLine(from, to);
			//				graphics.drawText(v.getName(), to);
			//			}

			int pIndex = v.isOutOfBounds() ? Math.min(Math.max(i+1, 0),N+1) : i+1;
			String text = v.isOutOfBounds() ? v.getName() + "=" + i : v.getName();

			Point from = positions.get(pIndex).getLocation().getTranslated(pWidth - FigureUtilities.getTextWidth(text, font)/2, y);
			//			if(!v.isBar()) {
			graphics.drawText(text, from);
			//				Dimension box = TextUtilities.INSTANCE.getTextExtents(text, graphics.getFont()).expand(10, 10);
			//				if(v.isIllegalAccess()) {
			//					graphics.setForegroundColor(ColorConstants.red);
			//					graphics.drawOval(from.x-5, from.y-5, box.width, box.height);
			//				}
			//			}

			//			List<Integer> indexes = v.getIndexes();
			//			for(int iOld = 0; iOld < indexes.size()-1; iOld++) {
			//				Point p = positions.get(indexes.get(iOld)).getLocation().getTranslated(pWidth, y + vars.size()*ARROW_EDGE);
			//				graphics.drawOval(p.x-1, p.y+5, 2, 2);
			//			}

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
}
