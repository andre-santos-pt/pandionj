package pt.iscte.pandionj;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;

public class InvokeWidget extends Composite {
	private StackLayout layout;
	private Map<String, StaticInvocationWidget2> invWidgetsMap;


	InvokeWidget(Composite parent) {
		super(parent, SWT.NONE);
		layout = new StackLayout();
		setLayout(layout);
		invWidgetsMap = new WeakHashMap<>();
	}


	public void setMethod(IMethod method, InvocationAction a) {
		String key = null;
		try {
			IType type = (IType) method.getParent();
			key = type.getFullyQualifiedName() + "|" + method.getElementName() + method.getSignature();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		if(key != null) {
			StaticInvocationWidget2 inv = invWidgetsMap.get(key);
			if(inv == null) {
				inv = new StaticInvocationWidget2(this, null, method, a);
				invWidgetsMap.put(key, inv);
			}
			inv.refreshItems();
			layout.topControl = inv;
			layout();
		}
	}


	@Override
	public boolean setFocus() {
		if(layout.topControl != null)
			return layout.topControl.setFocus();
		else
			return false;
	}
}
