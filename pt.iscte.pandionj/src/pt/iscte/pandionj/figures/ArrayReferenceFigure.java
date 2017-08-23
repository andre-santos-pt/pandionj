package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.ARRAY_POSITION_SPACING;
import static pt.iscte.pandionj.Constants.OBJECT_CORNER;
import static pt.iscte.pandionj.Constants.POSITION_WIDTH;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;

public class ArrayReferenceFigure extends AbstractArrayFigure<IReferenceModel> {
	private final int N;
	private List<Position> positions;
	private RoundedRectangle positionsFig;

	private List<Anchor> anchors;
	

	public ArrayReferenceFigure(IArrayModel<IReferenceModel> model) {
		super(model);
		N = Math.min(model.getLength(), Constants.ARRAY_LENGTH_LIMIT);
		positions = new ArrayList<>(N);
		positionsFig = createPositionsFig();
		add(positionsFig);
		
		anchors = new ArrayList<>(positions.size());
		for(Position p : positions)
			anchors.add(new Anchor(this, p));
	}

	
	private RoundedRectangle createPositionsFig() {
		RoundedRectangle fig = new RoundedRectangle();
		fig.setOpaque(false);
		fig.setCornerDimensions(Constants.OBJECT_CORNER);
		fig.setBackgroundColor(Constants.Colors.OBJECT);
		
		GridLayout layout = new GridLayout(1, true);
		layout.verticalSpacing = Constants.ARRAY_POSITION_SPACING;
		layout.horizontalSpacing = 0;
		layout.marginWidth = ARRAY_POSITION_SPACING;
		fig.setLayoutManager(layout);
		
		fig.setToolTip(new Label("length = " + model.getLength()));
		if(N == 0) {
			Label empty = new Label("");
			GridData layoutData = new GridData(POSITION_WIDTH/2, POSITION_WIDTH+20);
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
			
//			int len = Math.min(N, Constants.ARRAY_LENGTH_LIMIT);
//			for(int i = 0; i < len - 1; i++) {
//				Position p = new Position(i);
//				fig.add(p);
//				positions.add(p);
//			}
//			if(model.getLength() > Constants.ARRAY_LENGTH_LIMIT) {
//				Position emptyPosition = new Position(null);
//				fig.add(emptyPosition);
//			}
//			
//			Position lastPosition = new Position(model.getLength() - 1);
//			fig.add(lastPosition);
//			positions.add(lastPosition);
		}
		return fig;
	}
	

	public int convertToPositionFigureIndex(int i) {
		assert getModel().isValidModelIndex(i);
		int len = getModel().getLength();
		return len > 0 && i == len-1 ? positions.size()-1 : i;
	}
	
	public AbstractConnectionAnchor getAnchor(int modelIndex) {
		assert getModel().isValidModelIndex(modelIndex);
		return anchors.get(convertToPositionFigureIndex(modelIndex));
	}

	
	
	private class Position extends Figure {
		private ValueLabel valueLabel;
		private Label indexLabel;

		public Position(Integer index) {
			GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			GridData layoutData = new GridData(Constants.POSITION_WIDTH/2, Constants.POSITION_WIDTH*2);
			GridLayout layout = new GridLayout(1, false);
			layout.verticalSpacing = 5;
			layout.horizontalSpacing = 5;
			layout.marginWidth = 5;
			layout.marginHeight = 5;

			indexLabel = new Label(indexText(index));
			FontManager.setFont(indexLabel, Constants.INDEX_FONT_SIZE);
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);

			setLayoutManager(layout);
//			valueLabel = new Label("");
//			valueLabel.setBackgroundColor(Constants.Colors.ARRAY_POSITION);
//			valueLabel.setOpaque(true);
			valueLabel = new ValueLabel(indexText(index));
			layout.setConstraint(valueLabel, layoutData);
		}

		private String indexText(Integer index) {
			if(index == null) return "...";
			else if(index == ArrayReferenceFigure.this.N) return index + " (length)";
			else return Integer.toString(index);
		}

		
	}

	@Override
	public Rectangle getPositionBounds(int i) {
		Rectangle r = getBounds();
		if(i >= 0 && i < positions.size())
			r = positions.get(i).getBounds();
		translateToAbsolute(r);
		return r;
	}
	
	private static class Anchor extends AbstractConnectionAnchor {
		Position position;
		
		public Anchor(IFigure fig, Position p) {
			super(fig);
			position = p;
		}

		@Override
		public Point getLocation(Point reference) {
			Rectangle r = position.getBounds();
			getOwner().translateToAbsolute(r);
			return position.getLocation().translate(r.width/2, r.height/2);
		}
	}

}
