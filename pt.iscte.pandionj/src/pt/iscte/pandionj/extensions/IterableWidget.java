package pt.iscte.pandionj.extensions;

import java.util.Collection;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;

public class IterableWidget implements IObjectWidgetExtension {

	@Override
	public boolean accept(IType objectType) {
		 try {
			IType[] superInterfaces = objectType.newSupertypeHierarchy(null).getAllSuperInterfaces(objectType);
			for(IType t : superInterfaces)
				if(t.getFullyQualifiedName().equals(Collection.class.getName()))
					return true;
			return false;
		} catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public IFigure createFigure(IObjectModel e) {
		
		return new Label(e.getStringValue());
	}

	@Override
	public boolean includeMethod(String methodName) {
		return methodName.matches("size|isEmpty|add|remove");
	}
}
