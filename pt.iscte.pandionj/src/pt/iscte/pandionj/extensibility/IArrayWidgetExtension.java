package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;


public interface IArrayWidgetExtension extends IWidgetExtension<IArrayModel> {

	boolean accept(IArrayModel e);
	
	IFigure createFigure(IArrayModel e);
}
