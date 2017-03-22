package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;

public interface IWidgetExtension<M extends IEntityModel> {
	IFigure createFigure(M e);
}
