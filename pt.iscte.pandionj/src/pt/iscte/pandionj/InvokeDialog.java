package pt.iscte.pandionj;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;

public class InvokeDialog extends Dialog {
	private IMethod method;
	private InvocationAction action;
	private Button invokeButton;
	private StaticInvocationWidget invWidget;
	private String invocationExpression;
	private String[] paramValues;
	
	public InvokeDialog(Shell parentShell, IMethod method, InvocationAction action) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.method = method;
		this.action = action;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        invWidget = new StaticInvocationWidget(composite, this, method, action);
		return invWidget;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		invokeButton = createButton(parent, IDialogConstants.OK_ID, Constants.Messages.INVOKE, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialSize() {
		return super.getInitialSize();
	}
	
	void setValid(boolean valid, String invocationExpression, String[] paramValues) {
		if(invokeButton != null)
			invokeButton.setEnabled(valid);
		this.invocationExpression = invocationExpression;
		this.paramValues = paramValues;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		action.invoke(invocationExpression, paramValues);
		invWidget.setCache(paramValues);
	}

	@Override
	public int open() {
		return super.open();
	}

}