package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
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
		// TODO repor
//		e.invoke("toString", new IWatchExpressionListener() {
//			@Override
//			public void watchEvaluationFinished(IWatchExpressionResult result) {
//				PandionJUI.executeUpdate(() -> label.setText(result.getValue().getValueString()));
//			}
//		});
		return label;
	}

	@Override
	public boolean includeMethod(String methodName) {
		return false;
		//return methodName.matches("equals|compareTo");
	}
}
