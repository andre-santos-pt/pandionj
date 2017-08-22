package pt.iscte.pandionj.extensions;

import java.util.Collection;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.model.ModelObserver;

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
		Label label = new Label();
		e.registerDisplayObserver(new ModelObserver() {
			@Override
			public void update(Object arg) {
				eval(e, label);
			}
		});
		eval(e, label);
		return label;
	}

	private static void eval(IObjectModel e, Label label) {
		// TODO repor
//		e.invoke3("toArray", new IJavaValue[0], new IWatchExpressionListener() {
//			@Override
//			public void watchEvaluationFinished(IWatchExpressionResult result) {
//				IIndexedValue value = (IIndexedValue) result.getValue();
//				try {
//					IVariable[] positions = value.getSize() == 0 ? new IVariable[0] : value.getVariables(0, Math.min(value.getSize(), Constants.ARRAY_LENGTH_LIMIT));
//					String s = "{";
//					for(int i = 0; i < positions.length; i++)
//						s += positions[i].getValue().getValueString() + ", ";
//					s += "}";
//					String toString = s;
//					Display.getDefault().asyncExec(() -> {
//						label.setText(toString);
//					});
//				} catch (DebugException e1) {
//					e1.printStackTrace();
//				}
//
//			}
//		});
	}

	@Override
	public boolean includeMethod(String methodName) {
		return methodName.matches("size|isEmpty|add|remove");
	}
}
