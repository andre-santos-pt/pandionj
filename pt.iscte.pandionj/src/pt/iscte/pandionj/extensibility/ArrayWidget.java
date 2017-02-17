package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.Figure;

import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ModelElement;

public interface ArrayWidget {

	boolean qualifies(ArrayModel arrayModel);
	
	Figure createFigure(ArrayModel arrayModel);
	
	
}
