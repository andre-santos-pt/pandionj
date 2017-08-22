package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.getOneColGridLayout;
import static pt.iscte.pandionj.Constants.Colors.OBJECT;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;

//TODO limit size (to Constants.ARRAY_LENGTH_LIMIT)
public class ArrayReferenceFigure extends AbstractArrayFigure<IReferenceModel> {
	private final int N;
	private List<Position> positions;
	private GridLayout layout;
	private Figure positionsFig;


	public ArrayReferenceFigure(IArrayModel<IReferenceModel> model) {
		super(model);
		N = model.getLength();

		positions = new ArrayList<>(N);

		GridLayout layout = new GridLayout(1, true);
		setLayoutManager(layout);

		GridLayout layout2 = new GridLayout(1, true);
		layout2.horizontalSpacing = Constants.ARRAY_POSITION_SPACING;
		layout2.marginWidth = 0;

		//		setCornerDimensions(OBJECT_CORNER);
		setBackgroundColor(OBJECT);
		setOpaque(false);


		layout = getOneColGridLayout();
		setLayoutManager(layout);

		positionsFig = new Figure();
		positionsFig.setLayoutManager(layout2);
		positionsFig.setOpaque(false);
		add(positionsFig);

		setOpaque(false);

		for(int i = 0; i < N; i++) {
			Position p = new Position(i);
			positionsFig.add(p);
			positions.add(p);
		}
		Label lengthLabel = new Label("length = " + N);
		setToolTip(lengthLabel);
	}





	public AbstractConnectionAnchor getAnchor(int positionIndex) {
		return positions.get(positionIndex).anchor;
	}

	private class Position extends Figure {
		private final Label valueLabel;
		private Anchor anchor;

		public Position(Integer index) {
			anchor = new Anchor(ArrayReferenceFigure.this);

			GridData layoutCenter = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			GridData layoutData = new GridData(Constants.POSITION_WIDTH/2, Constants.POSITION_WIDTH*2);
			GridLayout layout = new GridLayout(1, false);
			layout.verticalSpacing = 5;
			layout.horizontalSpacing = 5;
			layout.marginWidth = 5;
			layout.marginHeight = 5;

			Label indexLabel = new Label(indexText(index));
			FontManager.setFont(indexLabel, Constants.INDEX_FONT_SIZE);
			indexLabel.setLabelAlignment(SWT.CENTER);
			indexLabel.setForegroundColor(ColorConstants.gray);
			layout.setConstraint(indexLabel, layoutCenter);
			add(indexLabel);

			setLayoutManager(layout);
			valueLabel = new Label("");
			valueLabel.setBackgroundColor(Constants.Colors.ARRAY_POSITION);
			valueLabel.setOpaque(true);
			layout.setConstraint(valueLabel, layoutData);
		}

		private String indexText(Integer index) {
			if(index == null) return "";
			else if(index == ArrayReferenceFigure.this.N) return index + " (length)";
			else return Integer.toString(index);
		}

		private class Anchor extends AbstractConnectionAnchor {
			public Anchor(IFigure fig) {
				super(fig);
			}

			@Override
			public Point getLocation(Point reference) {
				Rectangle r = Position.this.getBounds();
				getOwner().translateToAbsolute(r);
				return Position.this.getLocation().translate(r.width, r.height/2);
			}
		}
	}

	@Override
	public Rectangle getPositionBounds(int i) {
		// TODO Auto-generated method stub
		return null;
	}

}
