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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel;

public abstract class AbstractArrayFigure<E> extends PandionJFigure<IArrayModel<E>> {
	private final int N;
	final List<Position> positions;
	private final RoundedRectangle positionsFig;
	private final boolean horizontal;
	
	public AbstractArrayFigure(IArrayModel<E> model, boolean horizontal) {
		super(model, true);
		this.horizontal = horizontal;
		N = Math.min(model.getLength(), PandionJView.getMaxArrayLength());
		positions = new ArrayList<>(N);
		positionsFig = createPositionsFig();
		getLayoutManager().setConstraint(positionsFig, new GridData(SWT.DEFAULT, SWT.BEGINNING, false, false));
		add(positionsFig);
		model.getRuntimeModel().registerDisplayObserver((e) -> {
			if(e.type == IRuntimeModel.Event.Type.STEP) {
				positionsFig.setBackgroundColor(Constants.Colors.OBJECT);
			}
		});
	}

	abstract Figure createValueLabel(E e);
	abstract GridData createValueLabelGridData(boolean hole);
	abstract GridData createIndexLabelGridData();
	
	private RoundedRectangle createPositionsFig() {
		RoundedRectangle fig = new RoundedRectangle();
		fig.setOpaque(false);
		fig.setCornerDimensions(Constants.OBJECT_CORNER);
		fig.setBackgroundColor(Constants.Colors.OBJECT);
		
		GridLayout layout = new GridLayout(horizontal ? (model.getLength() > PandionJView.getMaxArrayLength() ? N + 1 : Math.max(1, N)) : 1, false);
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = Constants.ARRAY_POSITION_SPACING;
		layout.marginWidth = Constants.ARRAY_MARGIN;
		layout.marginHeight = Constants.ARRAY_MARGIN;
		fig.setLayoutManager(layout);
		
		fig.setToolTip(new Label("length = " + model.getLength()));
		if(N == 0) {
			Label empty = new Label("");
			GridData layoutData = new GridData(POSITION_WIDTH/2, POSITION_WIDTH);
			layout.setConstraint(empty, layoutData);
			fig.add(empty);
		}
		else {
			Iterator<Integer> it = model.getValidModelIndexes();
			while(it.hasNext()) {
				Integer i = it.next();
				if(!it.hasNext() && model.getLength() > PandionJView.getMaxArrayLength()) {
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
	
	@Override
	public void setBackgroundColor(Color color) {
		positionsFig.setBackgroundColor(color);
	}

	public Rectangle getPositionBounds(int i, boolean horizontal) {
		Rectangle r = getBounds();
		int len = model.getLength();
		if(len == 0)
			;
		else if(i < 0)
			r = positions.get(0).getBounds().getTranslated(
					horizontal ? -Constants.POSITION_WIDTH - Constants.ARRAY_MARGIN : 0, 
					horizontal ? 0 : -Constants.POSITION_WIDTH);
		else if(i >= len)
			r = positions.get(N-1).getBounds().getTranslated(
					horizontal ? Constants.POSITION_WIDTH + Constants.ARRAY_MARGIN : 0, 
					horizontal ? 0 : Constants.POSITION_WIDTH);
		else if(i == len - 1)
			r = positions.get(N-1).getBounds();
		else if(len > PandionJView.getMaxArrayLength() && i >= PandionJView.getMaxArrayLength()-1)
			r = positions.get(N-1).getBounds().getTranslated(
					horizontal ? -Constants.POSITION_WIDTH - Constants.ARRAY_POSITION_SPACING: 0, 
					horizontal ? 0 : -Constants.POSITION_WIDTH);
		else	 
			r = positions.get(i).getBounds();
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
				layout.verticalSpacing = 0;
				layout.horizontalSpacing = 0;
				layout.marginHeight = 0;
			}
			else{
				layout.verticalSpacing = 6;
				layout.horizontalSpacing = 3;
				layout.marginHeight = 4;
			}

			layout.marginWidth = 0;
			setLayoutManager(layout);

			if(index != null){
				valueLabel = createValueLabel(model.getElementModel(index));
			}else{
				valueLabel = new ValueLabel("...", false);
			}

			layout.setConstraint(valueLabel, createValueLabelGridData(index == null));
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
