package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.POSITION_WIDTH;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IArrayModel;

public abstract class AbstractArrayFigure<E> extends PandionJFigure<IArrayModel<E>> {
	private final int N;
	final List<Position> positions;
	private final RoundedRectangle positionsFig;
	private final boolean horizontal;
	
	public AbstractArrayFigure(IArrayModel<E> model, boolean horizontal) {
		super(model);
		this.horizontal = horizontal;
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT);
		positions = new ArrayList<>(N);
		positionsFig = createPositionsFig();
		add(positionsFig);
	}

	abstract Figure createValueLabel(E e);
	
	abstract GridData createValueLabelGridData();
	abstract GridData createIndexLabelGridData();
	
	private RoundedRectangle createPositionsFig() {
		RoundedRectangle fig = new RoundedRectangle();
		fig.setOpaque(false);
		fig.setCornerDimensions(Constants.OBJECT_CORNER);
		fig.setBackgroundColor(Constants.Colors.OBJECT);
		
		GridLayout layout = new GridLayout(horizontal ? (model.getLength() > Constants.ARRAY_LENGTH_LIMIT ? N + 1 : Math.max(1, N)) : 1, false);
		layout.verticalSpacing = Constants.ARRAY_POSITION_SPACING;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		fig.setLayoutManager(layout);
		
		fig.setToolTip(new Label("length = " + model.getLength()));
		if(N == 0) {
			Label empty = new Label("");
			GridData layoutData = new GridData(POSITION_WIDTH, POSITION_WIDTH+20);
			layout.setConstraint(empty, layoutData);
			fig.add(empty);
		}
		else {
			Iterator<Integer> it = model.getValidModelIndexes();
			while(it.hasNext()) {
				Integer i = it.next();
				if(!it.hasNext() && model.getLength() > Constants.ARRAY_LENGTH_LIMIT) {
					Position emptyPosition = new Position(null);
					fig.add(emptyPosition);
				}
				Position p = new Position(i);
				fig.add(p);
				positions.add(p);
			}
			
		}
		return fig;
	}
	

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
		translateToAbsolute(r);
		return r;
	}

	public Rectangle getLabelBounds(int i) {
		Rectangle r = getBounds();
		if(i >= 0 && i < model.getLength()){
			if(i < positions.size() - 2){
				r = positions.get(i).valueLabel.getBounds();
			}else if( i == model.getLength() - 1){
				r = positions.get(positions.size() - 1).valueLabel.getBounds();
			}else{
				r = positions.get(positions.size() - 2).valueLabel.getBounds();
			}
		}
		translateToAbsolute(r);
		return r;
	}

	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
		if(border != null)
			setSize(border.getPreferredSize(this));
	}
	
	class Position extends Figure {
		Figure valueLabel;
		private Label indexLabel;

		public Position(Integer index) {
			GridLayout layout = new GridLayout(horizontal ? 1 : 2, false);
			if(horizontal){
				layout.verticalSpacing = 3;
				layout.horizontalSpacing = 4;
				layout.marginHeight = 2;
			}
			else{
				layout.verticalSpacing = 6;
				layout.horizontalSpacing = 3;
				layout.marginHeight = 4;
			}

			layout.marginWidth = 4;
			setLayoutManager(layout);

			if(index != null){
				valueLabel = createValueLabel(model.getElementModel(index));
			}else{
				valueLabel = new ValueLabel("...", false);
			}

			layout.setConstraint(valueLabel, createValueLabelGridData());
			add(valueLabel);

			indexLabel = new ValueLabel(indexText(index), true);
			layout.setConstraint(indexLabel, createIndexLabelGridData());
			add(indexLabel, horizontal ? 1 : 0);
		}

		private String indexText(Integer index) {
			if(index == null) return "...";
			else return Integer.toString(index);
		}
	}
}
