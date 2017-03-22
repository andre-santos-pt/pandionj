package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;

public interface IObjectWidgetExtension extends IWidgetExtension<IObjectModel> {

	boolean accept(String objectType);
	
	IFigure createFigure(IObjectModel e);
}
