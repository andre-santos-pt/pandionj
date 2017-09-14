package pt.iscte.pandionj;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;

class InvocationArea extends Composite {

	StackLayout layout;
	Map<IMethod, StaticInvocationWidget> invWidgetsMap;
	Composite blank;
	
	InvocationArea(Composite parent) {
		super(parent, SWT.NONE);
		layout = new StackLayout();
		setLayout(layout);
		blank = new Composite(this, SWT.NONE);
		blank.setLayout(new FillLayout());
		new Label(blank, SWT.NONE).setText("File modified");
		layout.topControl = blank;
		invWidgetsMap = new WeakHashMap<>();
	}
	
	void setMethod(IFile file, IMethod method, InvocationAction a) {
		StaticInvocationWidget inv = invWidgetsMap.get(method);
		if(inv == null) {
			inv = new StaticInvocationWidget(this, file, method, a);
			invWidgetsMap.put(method, inv);
		}
		inv.refreshItems(file);
		layout.topControl = inv;
		layout();
	}
	
	void setBlank() {
		layout.topControl = blank;
		layout();
	}
	
	@Override
	public boolean setFocus() {
		if(layout.topControl != null)
			return layout.topControl.setFocus();
		else
			return false;
	}

}
