package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.IFigure;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;

public class Histogram implements IArrayWidgetExtension {

	@Override
	public boolean accept(IArrayModel e) {
		return e.getDimensions() == 1 && e.getComponentType().equals("double");
	}

	@Override
	public IFigure createFigure(IArrayModel e) {
		// TODO Auto-generated method stub
		return null;
	}

}
