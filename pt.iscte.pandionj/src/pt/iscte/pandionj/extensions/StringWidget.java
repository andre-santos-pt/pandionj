package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;

public class StringWidget implements IObjectWidgetExtension {

	@Override
	public boolean accept(IType objectType) {
		return objectType.getFullyQualifiedName().equals(String.class.getName());
	}

	@Override
	public IFigure createFigure(IObjectModel e) {
		Label label = new Label("\"" + e.getStringValue() + "\"");
		FontManager.setFont(label, Constants.VALUE_FONT_SIZE);
		return label;
	}

	@Override
	public boolean includeMethod(String methodName) {
		return methodName.matches("length|isEmpty|charAt|substring|concat|trim");
	}
}
