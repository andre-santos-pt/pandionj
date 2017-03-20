package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;

import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ModelElement;

public interface WidgetExtension {

	boolean accept(ArrayModel e);
	
	IFigure createFigure(ArrayModel e);

//	void positionChanged(Object oldValue, Object newValue, int i, int ... indexes);
	
}
