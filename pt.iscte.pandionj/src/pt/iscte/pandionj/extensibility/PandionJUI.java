package pt.iscte.pandionj.extensibility;


import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.ParamsDialog;

public interface PandionJUI {

	interface InvocationAction {
		void invoke(String expression);
	}
	
	static void promptInvocation(IMethod m, InvocationAction a) {
		if(m.getNumberOfParameters() != 0)
			PandionJView.getInstance().promptInvocation(m, a);
		
//		ParamsDialog dialog = new ParamsDialog(Display.getDefault().getActiveShell(), m);
//		if(dialog.open())
//			return dialog.getInvocationExpression();
		
	}
}
