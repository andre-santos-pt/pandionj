package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;

public class StringWidget implements IObjectWidgetExtension {

	@Override
	public boolean accept(String objectType) {
		return objectType.equals(String.class.getName());
	}

	@Override
	public IFigure createFigure(IObjectModel e) {
		
		Label label = new Label("\"" + e.getStringValue() + "\"");
		FontManager.setFont(label, Constants.VALUE_FONT_SIZE);
		return label;
	}

}
