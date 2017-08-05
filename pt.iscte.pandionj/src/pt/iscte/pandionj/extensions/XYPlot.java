package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.IFigure;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;

public class XYPlot implements IArrayWidgetExtension {
	@Override
	public boolean accept(IArrayModel e) {
		if(e.getDimensions() != 2 || !e.getComponentType().matches("double|float"))
			return false;
		
		return true;
	}
	@Override
	public IFigure createFigure(IArrayModel e) {
		return null;
	}
}
