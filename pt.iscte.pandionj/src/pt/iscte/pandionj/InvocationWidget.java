package pt.iscte.pandionj;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
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

public class InvocationWidget extends Composite {

	static Shell shell;
	static InvocationWidget area;

	static {
		shell = new Shell(Display.getDefault(), SWT.APPLICATION_MODAL);
		shell.setLayout(new FillLayout());
		area = new InvocationWidget(shell);
		shell.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				switch (event.detail) {
				case SWT.TRAVERSE_ESCAPE:
					shell.setVisible(false);
					event.detail = SWT.TRAVERSE_NONE;
					event.doit = false;
					break;
				}
			}
		});
	}

	private StackLayout layout;
	private Map<String, StaticInvocationWidget2> invWidgetsMap;


	private InvocationWidget(Composite parent) {
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
				inv = new StaticInvocationWidget2(this, method, a);
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

	public static void open(IMethod method, InvocationAction action) {
		area.setMethod(method, new InvocationAction() {
			@Override
			public void invoke(String expression, String[] paramValues) {
				action.invoke(expression, paramValues);
				shell.setVisible(false);
			}
		});

		shell.pack();
		Rectangle screen = Display.getCurrent().getClientArea();
		Point cursor = Display.getCurrent().getCursorLocation();
		int w = screen.width-cursor.x-shell.getSize().x-20;
		if(w < 0)
			cursor = new Point(cursor.x + w, cursor.y);
		shell.setLocation(cursor);
		if(shell.getVisible())
			shell.open();
		else
			shell.setVisible(true);

		shell.setFocus();
	}


}
