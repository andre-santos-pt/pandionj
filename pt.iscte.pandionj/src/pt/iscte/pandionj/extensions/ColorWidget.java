package pt.iscte.pandionj.extensions;

import java.awt.Color;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;

public class ColorWidget implements IObjectWidgetExtension {

	@Override
	public boolean accept(String objectType) {
		return objectType.equals(Color.class.getName().toString());
	}

	@Override
	public IFigure createFigure(IObjectModel e) {
		int r = 255; //e.getInt("getRed");
		Label label = new Label(r + ",");
		label.setOpaque(true);
		label.setBackgroundColor(new org.eclipse.swt.graphics.Color(null, r, r, r));
		return label;
	}

}
