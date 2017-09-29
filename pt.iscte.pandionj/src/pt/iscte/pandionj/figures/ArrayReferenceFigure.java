package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.POSITION_WIDTH;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;

public class ArrayReferenceFigure extends AbstractArrayFigure<IReferenceModel> {
	private List<Anchor> anchors;

	public ArrayReferenceFigure(IArrayModel<IReferenceModel> model) {
		super(model, false);
		anchors = new ArrayList<>(positions.size());
		for(Position p : positions)
			anchors.add(new Anchor(this, p));
	}

	public int convertToPositionFigureIndex(int i) {
		assert getModel().isValidModelIndex(i);
		int len = getModel().getLength();
		return len > 0 && i == len-1 ? positions.size()-1 : i;
	}
	
	public ConnectionAnchor getAnchor(int modelIndex) {
		assert getModel().isValidModelIndex(modelIndex);
		AbstractArrayFigure<IReferenceModel>.Position position = positions.get(convertToPositionFigureIndex(modelIndex));
		return ((ReferenceLabel) position.valueLabel).getAnchor();
//		return anchors.get(convertToPositionFigureIndex(modelIndex));
	}

	private static class Anchor extends AbstractConnectionAnchor {
		Position position;
		
		public Anchor(IFigure fig, Position p) {
			super(fig);
			position = p;
		}

		@Override
		public Point getLocation(Point reference) {
			Rectangle r = position.valueLabel.getBounds();
			getOwner().translateToAbsolute(r);
			return position.valueLabel.getLocation().translate(r.width/2, r.height/2);
		}
	}
	
	@Override
	Figure createValueLabel(IReferenceModel e) {
		return new ReferenceLabel(e);
	}

	@Override
	GridData createValueLabelGridData(boolean hole) {
		int w = hole ? POSITION_WIDTH/2 : POSITION_WIDTH;
		return new GridData(w, w);
	}
	
	@Override
	GridData createIndexLabelGridData() {
		return new GridData(POSITION_WIDTH/2, POSITION_WIDTH/2);
	}
	
	public ConnectionAnchor getIncommingAnchor() {
		return new CustomChopboxAnchor(this, (r) -> new Point(r.x + POSITION_WIDTH*2, r.y + POSITION_WIDTH));
	}
}