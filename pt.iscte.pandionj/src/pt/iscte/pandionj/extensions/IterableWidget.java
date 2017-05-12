package pt.iscte.pandionj.extensions;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.Constants;
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
		Label label = new Label();
		e.registerDisplayObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				eval(e, label);
			}
		});
		eval(e, label);
		return label;
	}

	private static void eval(IObjectModel e, Label label) {
		e.invoke3("toArray", new IJavaValue[0], new IWatchExpressionListener() {
			@Override
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				IIndexedValue value = (IIndexedValue) result.getValue();
				try {
					IVariable[] positions = value.getSize() == 0 ? new IVariable[0] : value.getVariables(0, Math.min(value.getSize(), Constants.ARRAY_LENGTH_LIMIT));
					String s = "{";
					for(int i = 0; i < positions.length; i++)
						s += positions[i].getValue().getValueString() + ", ";
					s += "}";
					String toString = s;
					Display.getDefault().asyncExec(() -> {
						label.setText(toString);
					});
				} catch (DebugException e1) {
					e1.printStackTrace();
				}

			}
		});
	}

	@Override
	public boolean includeMethod(String methodName) {
		return methodName.matches("size|isEmpty|add|remove");
	}
}
