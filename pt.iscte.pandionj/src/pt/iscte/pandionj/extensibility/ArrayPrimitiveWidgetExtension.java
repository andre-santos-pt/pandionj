package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.IFigure;

import pt.iscte.pandionj.model.ArrayPrimitiveModel;

public interface ArrayPrimitiveWidgetExtension {

	boolean accept(Object[] array, int dimensions);
	
	boolean qualifies(ArrayPrimitiveModel arrayModel);
	
	IFigure createFigure(ArrayPrimitiveModel arrayModel);


	
	void positionChanged(Object oldValue, Object newValue, int i, int indexes);
	
}
