package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.getOneColGridLayout;
import static pt.iscte.pandionj.Constants.Colors.OBJECT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.model.ArrayIndexVariableModel;

//TODO limit size (to Constants.ARRAY_LENGTH_LIMIT)
public class ArrayReferenceFigure extends PandionJFigure<IArrayModel<IReferenceModel>> {
	private final int N;
	private int lowerOffSet;
	private List<Position> positions;
	private Map<String, ArrayIndexVariableModel> vars;

	private GridLayout layout;

	private Figure positionsFig;

	
	public ArrayReferenceFigure(IArrayModel<IReferenceModel> model) {
		super(model);
		N = model.getLength();
		lowerOffSet = 0;

		positions = new ArrayList<>(N+2);
		vars = new HashMap<>();

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
		
		if(N == 0) {
			positionsFig.add(new Position(null, true));
		}
		else {
			for(int i = 0; i < N; i++) {
				Position p = new Position(i, false);
				positionsFig.add(p);
				positions.add(p);
			}
		}
		Label lengthLabel = new Label("length = " + N);
		setToolTip(lengthLabel);
	}
	

	private Position getPosition(int arrayIndex) {
		assert arrayIndex >= 0 && arrayIndex < N;
		return positions.get(lowerOffSet + arrayIndex);
	}



	public void addOutOfUpperBound(int index) {
		for(int i = 0; i < index-(N-1); i++) {
			Position p = new Position(positions.size(), true);
			positionsFig.add(p);
			positions.add(p);
		}
		changeLayout();
	}


	public void addOutOfLowerBound(int index) {
		for(int i = -1; i > index; i--) {
			Position p = new Position(i, true);
			positionsFig.add(p, 0);
			positions.add(p);
		}
		lowerOffSet = lowerOffSet + (lowerOffSet-index);
		changeLayout();
	}
	private void changeLayout() {
		layout = new GridLayout(positions.size(), true);
		layout.horizontalSpacing = Constants.ARRAY_POSITION_SPACING;
		positionsFig.setLayoutManager(layout);
		repaint();
	}


	public AbstractConnectionAnchor getAnchor(int positionIndex) {
		return positions.get(positionIndex).anchor;
	}
	
	private class Position extends Figure {
		private final Label valueLabel;
		private boolean outOfBounds;
		private Anchor anchor;
		
		public Position(Integer index, boolean outOfBounds) {
			this.outOfBounds = outOfBounds;
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
			if(!outOfBounds) {
				LineBorder lineBorder = new LineBorder(ColorConstants.black, Constants.POSITION_LINE_WIDTH, Graphics.LINE_SOLID);
				valueLabel.setBorder(lineBorder);
			}
			layout.setConstraint(valueLabel, layoutData);
//			add(valueLabel); // ?


		}

		private String indexText(Integer index) {
			if(index == null) return "";
			else if(index == ArrayReferenceFigure.this.N) return index + " (length)";
			else return Integer.toString(index);
		}
		
		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);
			if(outOfBounds) {
				graphics.setForegroundColor(ColorConstants.gray);
				graphics.setLineDashOffset(2.5f);
				graphics.setLineStyle(Graphics.LINE_DASH);
				graphics.drawRectangle(getLocation().x, getLocation().y, Constants.POSITION_WIDTH/2, Constants.POSITION_WIDTH-1);
			}
		}
		
		
		
		private class Anchor extends AbstractConnectionAnchor {
			public Anchor(IFigure fig) {
				super(fig);
			}

			@Override
			public Point getLocation(Point reference) {
//				org.eclipse.draw2d.geometry.Rectangle r =  org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
//				r.setBounds(getOwner().getBounds());
//				r.translate(0, 0);
//				r.resize(1, 1);
//				getOwner().translateToAbsolute(r);
				Rectangle r = Position.this.getBounds();
				getOwner().translateToAbsolute(r);
				return Position.this.getLocation().translate(r.width, r.height/2);
			}
		}
	}
	
}
