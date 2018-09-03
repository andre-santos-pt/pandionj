package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;

public interface IWidgetExtension<M> {
	IFigure createFigure(M e, IPropertyProvider args);
}
