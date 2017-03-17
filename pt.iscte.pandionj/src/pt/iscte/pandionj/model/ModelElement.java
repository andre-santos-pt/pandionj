package pt.iscte.pandionj.model;

import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

public interface ModelElement {
	IJavaValue getContent();
	void update();
	void registerObserver(Observer o);
	
	IFigure createFigure(Graph graph);
}
