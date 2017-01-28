package pt.iscte.pandionj.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaValue;

public interface ModelElement {
	void update();
	IJavaValue getContent();
	IFigure createFigure();
}
