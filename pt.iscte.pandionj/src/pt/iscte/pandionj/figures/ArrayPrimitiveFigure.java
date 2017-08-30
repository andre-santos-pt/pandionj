package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.POSITION_WIDTH;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IValueModel;

public class ArrayPrimitiveFigure extends AbstractArrayFigure<IValueModel> {
	public ArrayPrimitiveFigure(IArrayModel<IValueModel> model) {
		super(model, true);
	}

	@Override
	Figure createValueLabel(IValueModel e) {
		return new ValueLabel(e);
	}

	@Override
	GridData createValueLabelGridData() {
		return new GridData(POSITION_WIDTH, POSITION_WIDTH);
	}
}