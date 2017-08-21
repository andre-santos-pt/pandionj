package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.Constants.INDEX_FONT_SIZE;
import static pt.iscte.pandionj.Constants.OBJECT_CORNER;
import static pt.iscte.pandionj.Constants.POSITION_WIDTH;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IValueModel;

public class ArrayPrimitiveFigure2 extends PandionJFigure<IArrayModel<IValueModel>> {
	private static final GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
//	private final IArrayModel<IValueModel> model; // array being displayed
	private final int N; // array length
	private List<Position> positions; // existing array positions

	private GridLayout outerLayout;
	private GridLayout arrayLayout;
	private RoundedRectangle positionsFig;

	private GridData positionLayout;
	
	public ArrayPrimitiveFigure2(IArrayModel<IValueModel> model) {
		super(model);
//		this.model = model;
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT);
		positions = new ArrayList<>(N+1);

		outerLayout = new GridLayout(1, false);
		setLayoutManager(outerLayout);

		setBackgroundColor(Constants.Colors.OBJECT);
		positionsFig = createPositionsFig();
		add(positionsFig);
		setSize(getPreferredSize());
		
		int width = POSITION_WIDTH;
		if(model.isDecimal())
			width *= 2;

		positionLayout = new GridData(width, POSITION_WIDTH+20);
	}

	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
		setSize(border.getPreferredSize(this));
	}
	
	public int getNumberOfPositions() {
		return N;
	}

	private RoundedRectangle createPositionsFig() {
		RoundedRectangle fig = new RoundedRectangle();
		arrayLayout = new GridLayout(Math.max(1, N+1), false);
		arrayLayout.horizontalSpacing = ARRAY_POSITION_SPACING;
		arrayLayout.verticalSpacing = 0;
		arrayLayout.marginHeight = ARRAY_POSITION_SPACING;
		
		fig.setLayoutManager(arrayLayout);
		fig.setCornerDimensions(OBJECT_CORNER);

		Label lengthLabel = new Label("length = " + model.getLength());
		fig.setToolTip(lengthLabel);
		if(N == 0) {
//			fig.setPreferredSize(new Dimension(Constants.POSITION_WIDTH/2,Constants.POSITION_WIDTH));
			Label empty = new Label("");
			GridData layoutData = new GridData(POSITION_WIDTH/2, POSITION_WIDTH+20);
			arrayLayout.setConstraint(empty, layoutData);
			fig.add(empty);
		}
		else {
			int len = Math.min(N, Constants.ARRAY_LENGTH_LIMIT);
			for(int i = 0; i < len - 1; i++) {
				Position p = new Position(i);
				fig.add(p);
				positions.add(p);
			}
			if(model.getLength() > Constants.ARRAY_LENGTH_LIMIT) {
				Position emptyPosition = new Position(null);
				fig.add(emptyPosition);
			}
			
			Position lastPosition = new Position(model.getLength()-1);
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
				int last = Math.min(index, Constants.ARRAY_LENGTH_LIMIT-1);
				IValueModel m = model.getElementModel(last); 
				valueLabel = new ValueLabel(m);
				layout.setConstraint(valueLabel, new GridData(width, POSITION_WIDTH));
				add(valueLabel);
			}else {
				Label emptyLabel = new Label("...");
				FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
				IValueModel measure = model.getElementModel(0);
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

	Rectangle getFirstPositionBounds() {
		return getPositionBounds(0);
	}
	
	Rectangle getLastPositionBounds() {
		return getPositionBounds(positions.size()-1);
	}
	
	Rectangle getPositionBounds(int i) {
		Rectangle r = getBounds();
		if(i >= 0 && i < positions.size())
			r = positions.get(i).getBounds();
		translateToAbsolute(r);
		return r;
	}
}