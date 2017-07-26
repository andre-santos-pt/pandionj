package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.Constants.INDEX_FONT_SIZE;
import static pt.iscte.pandionj.Constants.OBJECT_CORNER;
import static pt.iscte.pandionj.Constants.POSITION_WIDTH;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IVariableModel;

public class ArrayPrimitiveFigure2 extends Figure{
	private static final GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
	private final IArrayModel model; // array being displayed
	private final int N; // array length
	private List<Position> positions; // existing array positions

	private GridLayout outerLayout;
	private GridLayout arrayLayout;
	private RoundedRectangle positionsFig;

	public ArrayPrimitiveFigure2(IArrayModel model) {
		this.model = model;
		model.registerDisplayObserver((o, indexes) -> observerAction(o, indexes));
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT); // limit size
		positions = new ArrayList<>(N);

		setBackgroundColor(Constants.Colors.OBJECT);

		outerLayout = new GridLayout(1, false);
		setLayoutManager(outerLayout);

		positionsFig = createPositionsFig();
		add(positionsFig);
		setSize(getPreferredSize());
	}

	//	@Override
	//	public Dimension getPreferredSize(int wHint, int hHint) { 
	//		int varSpace = vars.size() * 30;
	//		return super.getPreferredSize(wHint, hHint).expand(0, varSpace);
	//		return new Dimension();
	//	}

	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
		setSize(border.getPreferredSize(this));
	}
	
	public int getArrayLength() {
		return N;
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
				IVariableModel m = model.getElementModel(index); 
				valueLabel = new ValueLabel(m);
				layout.setConstraint(valueLabel, new GridData(width, POSITION_WIDTH));
				add(valueLabel);
			}else {
				Label emptyLabel = new Label("...");
				FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
				IVariableModel measure = model.getElementModel(model.getLength() - 1);
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

	Point getFirstPositionLocation() {
		return getPositionLocation(0);
	}
	
	Point getLastPositionLocation() {
		return getPositionLocation(positions.size()-1);
	}
	
	Point getPositionLocation(int i) {
		if(i < 0 || i >= positions.size())
			return null;
		else {
			Point p = positions.get(i).getLocation();
			translateToAbsolute(p);
			return p;
		}
			
	}
	
	private void observerAction(Observable o, Object arg) {
		if(arg instanceof IndexOutOfBoundsException) {
			System.out.println("Index fora");
			//			updateOutOfBoundsPositions();
			//			for(IArrayIndexModel v : vars.values())
			//				if(isOutOfBounds(v.getCurrentIndex()))
			//					if(v.getCurrentIndex() < 0)
			//						leftBound.markError();
			//					else
			//						rightBound.markError();
		}
		//		else if(arg instanceof IArrayIndexModel) {
		//			addVariable((IArrayIndexModel) arg); 
		//		}
	}

}