package pt.iscte.pandionj;


import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;
import pt.iscte.pandionj.model.PrimitiveType;

public class StaticInvocationWidget extends Composite {

	private String methodName;
	private IMethod method;
	private Combo[] paramBoxes; 
	private String[] parameterTypes;
	private IFile file;
	private long fileStamp;
	private Label info;

	public StaticInvocationWidget(Composite parent, IFile file, IMethod method, InvocationAction action) {
		super(parent, SWT.NONE);
		this.method = method;
		this.file = file;
		this.fileStamp = file.getModificationStamp();

		methodName = method.getElementName();
		parameterTypes = method.getParameterTypes();
		String[] parameterNames = null;
		try {
			parameterNames = method.getParameterNames();
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		RowLayout rowLayout = new RowLayout();
		rowLayout.spacing = 5;
		rowLayout.marginTop = Constants.MARGIN;
		rowLayout.marginLeft = Constants.MARGIN;

		setLayout(rowLayout);
		Label label = new Label(this, SWT.NONE);
		FontManager.setFont(label, Constants.VAR_FONT_SIZE);

		label.setText(method.getElementName() + " (");
		paramBoxes = new Combo[method.getNumberOfParameters()];
		GridLayout fillLayout = new GridLayout(1, false);
		fillLayout.marginWidth = 3;
		fillLayout.marginHeight = 3;
		fillLayout.verticalSpacing = 1;
		
		for(int i = 0; i < parameterTypes.length; i++) {
			if(i != 0) {
				Label comma = new Label(this, SWT.NONE);
				FontManager.setFont(comma, Constants.VAR_FONT_SIZE);
				comma.setText(", ");
			}
			String pType = Signature.getSignatureSimpleName(parameterTypes[i]);
			Composite comboComp = new Composite(this, SWT.NONE);
			comboComp.setLayout(fillLayout);
			Combo combo = new Combo(comboComp, SWT.DROP_DOWN);
			comboComp.setToolTipText(pType);
			int comboWidth = pType.equals(String.class.getSimpleName()) ? Constants.COMBO_STRING_WIDTH : Constants.COMBO_WIDTH; 
			combo.setLayoutData(new GridData(comboWidth, SWT.DEFAULT));
			
			Label varName = new Label(comboComp, SWT.NONE);
			varName.setText(parameterNames[i]);
			FontManager.setFont(varName, Constants.MESSAGE_FONT_SIZE);
			varName.setForeground(Constants.Colors.ROLE_ANNOTATIONS);
			
			addCombovalues(combo, parameterTypes[i]);

			if(combo.getItemCount() == 0)
				combo.setText(defaultItem(pType));

			paramBoxes[i] = combo;

			int ii = i;
			combo.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if((e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) && allValid()) {
						invokeOrNext(action, ii);
					}
				}
				@Override
				public void keyReleased(KeyEvent e) {
					boolean valid = valid(combo, pType);
					combo.getParent().setBackground(valid ? null : Constants.Colors.ERROR);
					varName.setForeground(valid ? Constants.Colors.ROLE_ANNOTATIONS : Constants.Colors.VIEW_BACKGROUND);
					comboComp.setToolTipText(valid ? pType : pType + ": inserted value not compatible");
					info.setVisible(allValid());
				}
			});
		}
		Label close = new Label(this, SWT.NONE);
		FontManager.setFont(close, Constants.VAR_FONT_SIZE);
		close.setText(")");
		info = new Label(this, SWT.NONE);
		info.setText(Constants.Messages.PRESS_TO_INVOKE);
		info.setForeground(Constants.Colors.CONSTANT);
	}

	private void invokeOrNext(InvocationAction action, int i) {
		for(int j = 0; j < paramBoxes.length; j++) {
			if(PrimitiveType.isPrimitiveSig(parameterTypes[i]))
				if(!containsItem(paramBoxes[i], paramBoxes[i].getText()))
					paramBoxes[i].add(paramBoxes[i].getText());
		}
		if(fileStamp == file.getModificationStamp()) {
			String exp = getInvocationExpression();
			action.invoke(exp, null);
		}
	}

	public void refreshItems(IFile file) {
		fileStamp = file.getModificationStamp();
		for(int i = 0; i < paramBoxes.length; i++)
			addCombovalues(paramBoxes[i], parameterTypes[i]);
	}

	private void addCombovalues(Combo combo, String paramType) {
		if(!PrimitiveType.isPrimitiveSig(paramType)) {
			String sel = combo.getText();
			combo.removeAll();
			combo.add("null");
			IType owner = (IType) method.getParent();
			try {
				IField[] fields = owner.getFields();
				for(IField f : fields)
					if(Flags.isStatic(f.getFlags()) && f.getTypeSignature().equals(paramType))
						combo.add(f.getElementName());


			} catch (JavaModelException e1) {
				e1.printStackTrace();
			}
			if(sel.isEmpty())
				combo.select(0);
			else
				combo.setText(sel);
		}
	}

	private boolean containsItem(Combo combo, String item) {
		for(int i = 0; i < combo.getItemCount(); i++)
			if(combo.getItem(i).equals(item))
				return true;

		return false;
	}


	private boolean allValid() {
		String[] parameterTypes = method.getParameterTypes();
		boolean allValid = true;
		for(int i = 0; i < paramBoxes.length; i++) {
			boolean v = valid(paramBoxes[i], Signature.getSignatureSimpleName(parameterTypes[i]));
			if(!v)
				allValid = false;
		}

		return allValid;
	}

	private boolean valid(Combo combo, String pType) {
		return validValue(combo.getText(), pType) ||  containsItem(combo, combo.getText());
	}

	private boolean validValue(String val, String pType) {
		try {
			if(pType.equals(String.class.getSimpleName())) return val.matches("(\"(.)*\")|null");
			else if(pType.equals(char.class.getName())) return val.matches("'.'");
			else if(pType.equals(boolean.class.getName())) return  val.matches("true|false");
			else if(pType.equals(byte.class.getName())) Byte.parseByte(val);
			else if(pType.equals(short.class.getName())) Short.parseShort(val);
			else if(pType.equals(int.class.getName())) Integer.parseInt(val);
			else if(pType.equals(long.class.getName())) Long.parseLong(val);
			else if(pType.equals(float.class.getName())) Float.parseFloat(val);
			else if(pType.equals(double.class.getName())) Double.parseDouble(val);
			else return false;
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
		if(pType.equals(float.class.getName())) 		return "0.0f";
		if(pType.equals(double.class.getName())) 	return "0.0";
		if(pType.equals(char.class.getName())) 		return "'a'";
		if(pType.equals(boolean.class.getName())) 	return "false";

		if(pType.equals(String.class.getSimpleName())) 	return "\"\"";

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
		String[] parameterTypes = method.getParameterTypes();

		for(int i = 0; i < values.length; i++)
			if(!contains(paramBoxes[i].getItems(), values[i]))
				paramBoxes[i].add(values[i]);

		for(int i = 0; i < values.length; i++) {
			String pType = Signature.getSignatureSimpleName(parameterTypes[i]);
			values[i] = convertForTypedInvocation(values[i], pType);
		}

		return methodName + "(" + String.join(", ", values) + ")";
	}

	private String convertForTypedInvocation(String val, String pType) {
		if(pType.matches("byte|short|long"))
			return "(" + pType + ")" + val;
		if(pType.equals(float.class.getName()) && !val.endsWith("f"))
			return val + "f";
		else if(pType.equals(double.class.getName()) && val.indexOf('.') == -1)
			return val + ".0";
		else
			return val;
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
		return true;
	}
}