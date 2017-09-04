package pt.iscte.pandionj.extensions;

import java.awt.Color;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;

public class ColorWidget implements IObjectWidgetExtension {

	@Override
	public boolean accept(IType objectType) {
		return objectType.getFullyQualifiedName().equals(Color.class.getName());
	}

	@Override
	public IFigure createFigure(IObjectModel e) {
		int value = e.getInt("value");
		int r = (value >> 16) & 0xFF;
		int g = (value >> 8) & 0xFF;
		int b = (value >> 0) & 0xFF;
		Label label = new Label(r + ", " + g + ", " + b);
		label.setOpaque(true);
		label.setBackgroundColor(new org.eclipse.swt.graphics.Color(null, r, g, b));
		return label;
	}
	
	@Override
	public boolean includeMethod(String methodName) {
		return false;
	}

}
