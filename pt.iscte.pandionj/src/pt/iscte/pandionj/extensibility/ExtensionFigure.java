package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.Figure;

public abstract class ExtensionFigure extends Figure {

	public abstract void positionChanged(Object oldValue, Object newValue, int i, int indexes);
}
