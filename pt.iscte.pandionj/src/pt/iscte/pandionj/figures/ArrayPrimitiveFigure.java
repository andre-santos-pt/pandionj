package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.extensibility.PandionJConstants.POSITION_WIDTH;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public class ArrayPrimitiveFigure extends AbstractArrayFigure<IValueModel> {
	public ArrayPrimitiveFigure(IArrayModel<IValueModel> model) {
		super(model, true);
	}

	@Override
	Figure createValueLabel(IValueModel e) {
		return new ValueLabel(e);
	}

	@Override
	GridData createValueLabelGridData(boolean hole) {
		return new GridData(POSITION_WIDTH, POSITION_WIDTH);
	}
	
	@Override
	GridData createIndexLabelGridData() {
		return new GridData(SWT.CENTER, SWT.BEGINNING, false, false);
	}
	
	public ConnectionAnchor getIncommingAnchor() {
		return new CustomChopboxAnchor(this, (r) -> new Point(
				(int) Math.round(r.x + PandionJConstants.POSITION_WIDTH*2), 
				(int) Math.round(r.y + (r.height - 20) / 2.0)));
	}
}