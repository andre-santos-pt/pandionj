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

public class ArrayPrimitiveFigure extends AbstractArrayFigure<IValueModel> {
	private static final GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
	private final int N; // array length
	private List<Position> positions; // existing array positions

	private GridLayout arrayLayout;
	private RoundedRectangle positionsFig;
	
	public ArrayPrimitiveFigure(IArrayModel<IValueModel> model) {
		super(model);
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT);
		positions = new ArrayList<>(N);
	
		positionsFig = createPositionsFig();
		add(positionsFig);
		setSize(getPreferredSize());
	}

	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
		if(border != null)
			setSize(border.getPreferredSize(this));
	}
	
	public int getNumberOfPositions() {
		return N;
	}

	private RoundedRectangle createPositionsFig() {
		RoundedRectangle fig = new RoundedRectangle();
		fig.setBackgroundColor(Constants.Colors.OBJECT);
		arrayLayout = new GridLayout(Math.min(Math.max(1, N+1),Constants.ARRAY_LENGTH_LIMIT), true);
		arrayLayout.horizontalSpacing = ARRAY_POSITION_SPACING;
		arrayLayout.verticalSpacing = 0;
		arrayLayout.marginHeight = ARRAY_POSITION_SPACING;
		
		fig.setLayoutManager(arrayLayout);
		fig.setCornerDimensions(OBJECT_CORNER);

		fig.setToolTip(new Label("length = " + model.getLength()));
		if(N == 0) {
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
				positions.add(emptyPosition);
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
				IValueModel m = model.getElementModel(index); 
				valueLabel = new ValueLabel(m);
				layout.setConstraint(valueLabel, new GridData(width, POSITION_WIDTH));
				add(valueLabel);
			}else {
				valueLabel = new ValueLabel("...");
				layout.setConstraint(valueLabel, new GridData(width, POSITION_WIDTH));
				add(valueLabel);
			}

			indexLabel = new Label(index == null ? "..." : Integer.toString(index));
			FontManager.setFont(indexLabel, INDEX_FONT_SIZE);
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);
		}
	}

	// TODO testar
	public Rectangle getPositionBounds(int i) {
		Rectangle r = getBounds();
		if(i >= 0 && i < model.getLength()){
			if(i < positions.size() - 2){
				r = positions.get(i).getBounds();
			}else if( i == model.getLength() - 1){
				r = positions.get(positions.size() - 1).getBounds();
			}else{
				r = positions.get(positions.size() - 2).getBounds();
			}
		}
//		if(i >= 0 && i < positions.size())
//			r = positions.get(i).getBounds();
		translateToAbsolute(r);
		return r;
	}
}