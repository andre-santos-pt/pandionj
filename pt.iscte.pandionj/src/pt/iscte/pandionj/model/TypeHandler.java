package pt.iscte.pandionj.model;

import org.eclipse.draw2d.Figure;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;

public interface TypeHandler {
	boolean qualifies(IJavaValue e);
	boolean isValueType();
	boolean expandLinks(ModelElement e);
	Figure createFigure(ModelElement e);
	String getTextualValue(IJavaValue v);
}
