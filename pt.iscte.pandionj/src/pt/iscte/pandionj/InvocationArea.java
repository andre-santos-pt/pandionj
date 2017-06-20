package pt.iscte.pandionj;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;

class InvocationArea extends Composite {

	StackLayout layout;
	Map<IMethod, InvocationWidget> invWidgetsMap;

	InvocationArea(Composite parent) {
		super(parent, SWT.NONE);
		layout = new StackLayout();
		setLayout(layout);
		invWidgetsMap = new WeakHashMap<>();
	}
	
	void setMethod(IMethod method, InvocationAction a) {
		InvocationWidget inv = invWidgetsMap.get(method);
		if(inv == null) {
			inv = new InvocationWidget(this, method, a);
			invWidgetsMap.put(method, inv);
		}
		layout.topControl = inv;
		layout();
	}
	
	@Override
	public boolean setFocus() {
		if(layout.topControl != null)
			return ((InvocationWidget) layout.topControl).setFocus();
		else
			return false;
	}

}
