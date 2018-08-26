package pt.iscte.pandionj.testplug;


import java.awt.Color;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.ITypeWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class ColorAwtWidget implements ITypeWidgetExtension {

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
//		Label label = new Label(r + ", " + g + ", " + b);
		Label label = new Label("       ");
		label.setOpaque(true);
		label.setBackgroundColor(PandionJUI.getColor(r, g, b));
		label.setBounds(new Rectangle(0, 0, 100, 100));
		return label;
	}
	
	@Override
	public boolean includeMethod(String methodName) {
		return false;
	}

}
