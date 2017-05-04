package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;

public class ColorAguia implements IObjectWidgetExtension {

	@Override
	public boolean accept(IType objectType) {
		try {
			IType[] supertypes = objectType.newSupertypeHierarchy(null).getSupertypes(objectType);
			for(IType t : supertypes)
				return t.getFullyQualifiedName().equals("aguiaj.draw.IColor");
			return false;
		} catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public IFigure createFigure(IObjectModel e) {
		Label label = new Label("?");
		label.setBackgroundColor(ColorConstants.blue);
		label.setOpaque(true);
		return label;
	}

}
