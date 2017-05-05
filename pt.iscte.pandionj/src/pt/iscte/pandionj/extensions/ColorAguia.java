package pt.iscte.pandionj.extensions;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import pt.iscte.pandionj.ColorManager;
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
	public IFigure createFigure(IObjectModel m) {
		Label label = new Label("?");
		label.setBackgroundColor(ColorConstants.blue);
		label.setOpaque(true);
		m.registerDisplayObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				label.setBackgroundColor(ColorManager.getColor(m.getInt("r"), m.getInt("g"), m.getInt("b")));
			}
		});
		return label;
	}

}
