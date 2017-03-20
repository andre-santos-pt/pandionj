package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;

public interface ArrayWidgetExtension {

	boolean accept(Object[] array, String type, int dimensions);
	
	IFigure createFigure(Object[] array, String type, int dimensions);

//	void positionChanged(Object oldValue, Object newValue, int i, int ... indexes);
	
}
