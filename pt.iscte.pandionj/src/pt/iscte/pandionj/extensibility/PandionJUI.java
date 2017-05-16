package pt.iscte.pandionj.extensibility;


import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.ParamsDialog;

public interface PandionJUI {

	static String promptInvocationExpression(IMethod m) {
		if(m.getNumberOfParameters() == 0)
			return m.getElementName() + "()";
		
		ParamsDialog dialog = new ParamsDialog(Display.getDefault().getActiveShell(), m);
		if(dialog.open())
			return dialog.getInvocationExpression();
		
		return null;
	}
}
