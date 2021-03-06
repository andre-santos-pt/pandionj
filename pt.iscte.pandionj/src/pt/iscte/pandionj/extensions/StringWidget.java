package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;

import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IPropertyProvider;
import pt.iscte.pandionj.extensibility.ITypeWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public class StringWidget implements ITypeWidgetExtension {

	@Override
	public boolean accept(IType objectType) {
		return objectType.getFullyQualifiedName().equals(String.class.getName());
	}

	@Override
	public IFigure createFigure(IObjectModel e, IPropertyProvider args) {
		Label label = new Label("\"" + e.getStringValue() + "\"");
		FontManager.setFont(label, PandionJConstants.VALUE_FONT_SIZE);
		label.setForegroundColor(ColorConstants.black);
		return label;
	}

	@Override
	public boolean includeMethod(String methodName) {
		return methodName.matches("length|isEmpty|charAt|substring|concat|trim");
	}
}
