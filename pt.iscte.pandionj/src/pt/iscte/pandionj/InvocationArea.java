package pt.iscte.pandionj;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;

class InvocationArea extends Composite {
	StackLayout layout;
	Map<String, StaticInvocationWidget> invWidgetsMap;
	Composite blank;

	InvocationArea(Composite parent) {
		super(parent, SWT.NONE);
		layout = new StackLayout();
		setLayout(layout);
		blank = createFileModifiedView();
		layout.topControl = blank;
		invWidgetsMap = new WeakHashMap<>();
	}

	private Composite createFileModifiedView() {
		Composite blank = new Composite(this, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginWidth = Constants.MARGIN;
		fillLayout.marginHeight = Constants.MARGIN;
		blank.setLayout(fillLayout);
		Label label = new Label(blank, SWT.NONE);
		label.setText("File modified");
		FontManager.setFont(label, Constants.MESSAGE_FONT_SIZE);
		label.setForeground(Constants.Colors.ERROR);
		return blank;
	}

	void setMethod(IFile file, IMethod method, InvocationAction a) {
		String key = null;
		try {
			IType type = (IType) method.getParent();
			key = type.getFullyQualifiedName() + "|" + method.getElementName() + method.getSignature();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		if(key != null) {
			StaticInvocationWidget inv = invWidgetsMap.get(key);
			if(inv == null) {
				inv = new StaticInvocationWidget(this, file, method, a);
				invWidgetsMap.put(key, inv);
			}
			inv.refreshItems(file);
			layout.topControl = inv;
			layout();
		}
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
