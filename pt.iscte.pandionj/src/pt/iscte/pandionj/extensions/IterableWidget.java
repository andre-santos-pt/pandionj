package pt.iscte.pandionj.extensions;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;
import pt.iscte.pandionj.extensibility.IPropertyProvider;
import pt.iscte.pandionj.extensibility.ITypeWidgetExtension;
import pt.iscte.pandionj.extensibility.ModelObserver;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.ArrayReferenceModel;

public class IterableWidget implements ITypeWidgetExtension {

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
	public IFigure createFigure(IObjectModel e, IPropertyProvider args) {
		return ITypeWidgetExtension.NULL_EXTENSION.createFigure(e, args);
//		Label label = new Label();
//		e.registerDisplayObserver(new ModelObserver<Object>() {
//			@Override
//			public void update(Object arg) {
//				eval(e, label);
//			}
//		});
//		eval(e, label);
//		return label;
	}

	private static void eval(IObjectModel e, Label label) {
		e.invoke("toArray", new InvocationResult() {
			@Override
			public void valueReturn(Object o) {
				ArrayReferenceModel array = (ArrayReferenceModel) o;
				Iterator<Integer> indexes = array.getValidModelIndexes();
				
				String s = "{";
				Integer i = -1;
				while(indexes.hasNext()) {
					Integer j = indexes.next();
					if(j != 0)
						s += ", ";
					else if(i+1 != j)
						s += ", ..., ";
					i = j;
					try {
						s += array.getElementString(i); // FIXME problema index limite e com objetos (String)
					} catch (DebugException e) {
						s += "?";
					}
				}
				String text = s + "}";
				PandionJUI.executeUpdate(() -> {
					label.setText(text);
				});
			}
		});
	}

	@Override
	public boolean includeMethod(String methodName) {
		return methodName.matches("size|isEmpty|add|remove");
	}
}
