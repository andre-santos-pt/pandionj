package pt.iscte.pandionj.extensions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;

public class NumberWidget implements IObjectWidgetExtension {

	@Override
	public boolean accept(IType objectType) {
		try {
			String superClass = objectType.getSuperclassName();
			return Number.class.getName().equals(superClass);
		} catch (JavaModelException e) {
			return false;
		}
	}

	@Override
	public IFigure createFigure(IObjectModel e) {
		Label label = new Label();
		FontManager.setFont(label, Constants.VALUE_FONT_SIZE);
		e.invoke("toString", new IWatchExpressionListener() {
			@Override
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				PandionJView.executeUpdate(() -> label.setText(result.getValue().getValueString()));
			}
		});
		return label;
	}

	@Override
	public boolean includeMethod(String methodName) {
		return false;
		//return methodName.matches("equals|compareTo");
	}
}
