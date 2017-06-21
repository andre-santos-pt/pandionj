package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;

public class HistogramWidget implements IArrayWidgetExtension {

	@Override
	public boolean accept(IArrayModel e) {
		return e.getDimensions() == 1 && e.getComponentType().matches("short|int|lon|double|float");
	}

	@Override
	public IFigure createFigure(IArrayModel e) {
		RectangleFigure r = new RectangleFigure();
		r.setSize(50, 100);
		r.setBackgroundColor(ColorConstants.red);
		return r;
	}

}
