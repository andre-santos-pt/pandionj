package pt.iscte.pandionj;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;

public class InvocationWidget extends Composite {
	private Combo[] paramBoxes; 
	private String methodName;
	private IMethod method;


	public InvocationWidget(Composite parent, IMethod method, InvocationAction action) {
		super(parent, SWT.NONE);
		this.method = method;
		methodName = method.getElementName();
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = Constants.MARGIN;
		rowLayout.marginLeft = Constants.MARGIN;

		setLayout(rowLayout);
		org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(this, SWT.NONE);
		FontManager.setFont(label, Constants.MESSAGE_FONT_SIZE);

		label.setText(method.getElementName() + " (");

		paramBoxes = new Combo[method.getNumberOfParameters()];
		for(int i = 0; i < method.getNumberOfParameters(); i++) {
			if(i != 0) {
				org.eclipse.swt.widgets.Label comma = new org.eclipse.swt.widgets.Label(this, SWT.NONE);
				FontManager.setFont(comma, Constants.MESSAGE_FONT_SIZE);
				comma.setText(", ");
			}

			String pType = Signature.toString(method.getParameterTypes()[i]);
			Combo combo = new Combo(this, SWT.DROP_DOWN);
			combo.setToolTipText(pType);
			FontManager.setFont(combo, Constants.MESSAGE_FONT_SIZE);
			int comboWidth = pType.equals(String.class.getSimpleName()) ? Constants.COMBO_STRING_WIDTH : Constants.COMBO_WIDTH; 
			combo.setLayoutData(new RowData(comboWidth, SWT.DEFAULT));
			IType owner = (IType) method.getParent();
			try {
				IField[] fields = owner.getFields();
				for(IField f : fields)
					if(Flags.isStatic(f.getFlags()) && f.getTypeSignature().equals(method.getParameterTypes()[i]))
						combo.add(f.getElementName());

			} catch (JavaModelException e1) {
				e1.printStackTrace();
			}

			if(combo.getItemCount() == 0)
				combo.setText(defaultItem(pType));

			paramBoxes[i] = combo;

			int ii = i;
			combo.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					combo.setForeground(valid(combo, pType) ? null : Constants.Colors.ERROR);
				}
			});
			combo.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.CR) {
						if(ii == paramBoxes.length-1) {
							if(allValid())
								action.invoke(getInvocationExpression());
						}
						else {
							paramBoxes[ii+1].setFocus();
						}
					}
				}
			});
		}
		org.eclipse.swt.widgets.Label close = new org.eclipse.swt.widgets.Label(this, SWT.NONE);
		FontManager.setFont(close, Constants.MESSAGE_FONT_SIZE);
		close.setText(")");
	}

	// TODO review bugs
	private boolean allValid() {
		boolean allValid = true;
		for(int i = 0; i < paramBoxes.length; i++) {
			boolean v = valid(paramBoxes[i], Signature.getSignatureSimpleName(method.getParameterTypes()[i]));
			paramBoxes[i].setForeground(v ? ColorConstants.black : Constants.Colors.ERROR);
			if(!v)
				allValid = false;
		}

		return allValid;
	}

	// TODO review bugs
	private boolean valid(Combo combo, String pType) {
		String val = combo.getText();
		try {
			if(pType.equals(String.class.getSimpleName())) return val.matches("(\"(.)*\")|null");

			if(pType.equals(char.class.getName())) return val.matches("'.'");

			if(pType.equals(boolean.class.getName())) Boolean.parseBoolean(val);
			if(pType.equals(byte.class.getName())) Byte.parseByte(val);
			if(pType.equals(short.class.getName())) Short.parseShort(val);
			if(pType.equals(int.class.getName())) Integer.parseInt(val);
			if(pType.equals(long.class.getName())) Long.parseLong(val);
			if(pType.equals(float.class.getName())) Float.parseFloat(val);
			if(pType.equals(double.class.getName())) Double.parseDouble(val);

			// TODO arrays, null, refs
		}
		catch(RuntimeException e) {
			return false;
		}
		return true;
	}


	private String defaultItem(String pType) {
		if(pType.equals(byte.class.getName())) 		return "0";
		if(pType.equals(short.class.getName())) 		return "0";
		if(pType.equals(int.class.getName()))		return "0";
		if(pType.equals(long.class.getName())) 		return "0";
		if(pType.equals(float.class.getName())) 		return "0.0";
		if(pType.equals(double.class.getName())) 	return "0.0";
		if(pType.equals(char.class.getName())) 		return "'a'";
		if(pType.equals(boolean.class.getName())) 	return "false";

		if(pType.equals(String.class.getName())) 	return "\"\"";

		return "null";
	}


	public String[] getValues() {
		String[] values = new String[paramBoxes.length];
		for(int j = 0; j < values.length; j++)
			values[j] = paramBoxes[j].getText();
		return values;
	}

	public String getInvocationExpression() {
		String[] values = getValues();

		for(int i = 0; i < values.length; i++)
			if(!contains(paramBoxes[i].getItems(), values[i]))
				paramBoxes[i].add(values[i]);

		return methodName + "(" + String.join(",", values) + ")";
	}


	private static boolean contains(String[] items, String s) {
		for(String i : items)
			if(i.equals(s))
				return true;

		return false;
	}

	@Override
	public boolean setFocus() {
		paramBoxes[0].setFocus();
		pack();
		return true;
	}
}