package pt.iscte.pandionj;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;
import pt.iscte.pandionj.model.PrimitiveType;

public class StaticInvocationWidget extends Composite {

	private static final RowLayout rowLayout;
	private static final GridLayout comboLayout;

	private static Map<String, List<List<String>>> cache;

	static {
		rowLayout = new RowLayout();
		rowLayout.spacing = 5;
		rowLayout.marginTop = PandionJConstants.CANVAS_MARGIN;
		rowLayout.marginLeft = PandionJConstants.CANVAS_MARGIN;

		comboLayout = new GridLayout(1, false);
		comboLayout.marginWidth = 3;
		comboLayout.marginHeight = 3;
		comboLayout.verticalSpacing = 1;

		cache = new HashMap<>();
	}

	private String methodName;
	private IMethod method;
	private InvokeDialog invokeDialog;
	private Combo[] paramBoxes; 
	private String[] parameterTypes;

	public StaticInvocationWidget(Composite parent, InvokeDialog invokeDialog, IMethod method) {
		super(parent, SWT.NONE);
		this.invokeDialog = invokeDialog;
		
		this.method = method;

		setLayout(rowLayout);

		methodName = method.getElementName();
		parameterTypes = method.getParameterTypes();
		Label label = new Label(this, SWT.NONE);
		FontManager.setFont(label, PandionJConstants.VAR_FONT_SIZE);
		label.setText(method.getElementName() + " (");
		paramBoxes = new Combo[method.getNumberOfParameters()];
		String[] parameterNames = null;
		try {
			parameterNames = method.getParameterNames();
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		for(int i = 0; i < parameterTypes.length; i++) {
			if(i != 0) {
				Label comma = new Label(this, SWT.NONE);
				FontManager.setFont(comma, PandionJConstants.VAR_FONT_SIZE);
				comma.setText(", ");
			}
			paramBoxes[i] = createCombo(invokeDialog, parameterNames[i], parameterTypes[i]);
		}
		Label close = new Label(this, SWT.NONE);
		FontManager.setFont(close, PandionJConstants.VAR_FONT_SIZE);
		close.setText(")");

		addCacheValues(paramBoxes);
		checkValidity();
	}

	private Combo createCombo(InvokeDialog invokeDialog, String paramName, String paramType) {
		String pType = Signature.getSignatureSimpleName(paramType);
		Composite comboComp = new Composite(this, SWT.NONE);
		comboComp.setLayout(comboLayout);
		Combo combo = new Combo(comboComp, SWT.DROP_DOWN);
		combo.setToolTipText(pType);
		int comboWidth = pType.equals(String.class.getSimpleName()) || pType.endsWith("[]") ? PandionJConstants.COMBO_STRING_WIDTH : PandionJConstants.COMBO_WIDTH; 
		combo.setLayoutData(new GridData(comboWidth, SWT.DEFAULT));

		Label varName = new Label(comboComp, SWT.NONE);
		varName.setText(paramName);
		FontManager.setFont(varName, PandionJConstants.MESSAGE_FONT_SIZE);
		varName.setForeground(PandionJConstants.Colors.ROLE_ANNOTATIONS);
		varName.setToolTipText(pType);
		addRefCombovalues(combo, paramType);

		if(combo.getItemCount() == 0)
			combo.setText(defaultItem(pType));

		combo.addKeyListener(new KeyAdapter() {
			//				public void keyPressed(KeyEvent e) {
			//					if((e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) && allValid()) {
			//						invokeOrNext(action, ii);
			//					}
			//				}
			@Override
			public void keyReleased(KeyEvent e) {
				boolean valid = valid(combo, pType);
				varName.setForeground(valid ? PandionJConstants.Colors.ROLE_ANNOTATIONS : PandionJConstants.Colors.ERROR);
				varName.setToolTipText(valid ? pType : pType + ": inserted value not compatible");
				checkValidity();
			}
		});
		combo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkValidity();
			}
		});
		return combo;
	}

	private void checkValidity() {
		boolean allvalid = allValid();
		invokeDialog.setValid(allvalid, allvalid ? getInvocationExpression() : null, getValues());
	}
	
	
	private void addRefCombovalues(Combo combo, String paramType) {
		if(!PrimitiveType.isPrimitiveSig(paramType)) {
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
		}
	}

	private void addCacheValues(Combo[] combos) {
		String key = getMethodKey(method);
		List<List<String>> list = cache.get(key);
		if(list != null) {
			assert list.size() == combos.length;
			for(int i = 0; i < combos.length; i++) {
				List<String> values = list.get(i);
				for(String v : values)
					if(!containsItem(combos[i], v))
						combos[i].add(v);

				if(values.size() > 0)
					combos[i].select(combos[i].getItemCount()-1);
				else if(combos[i].getItemCount() > 0)
					combos[i].select(0);
			}
		}
		else {
			for(Combo combo : combos) {
				int n = combo.getItemCount();
				if(n > 0)
					combo.select(n-1);
			}
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
			String pType = Signature.getSignatureSimpleName(parameterTypes[i]);
			boolean v = valid(paramBoxes[i], pType);
			if(!v)
				allValid = false;
		}

		return allValid;
	}

	private boolean valid(Combo combo, String pType) {
		return validValue(combo.getText(), pType, combo) ||  containsItem(combo, combo.getText());
	}

	private static final String INT_REGEX = "\\-?\\s*\\d+";
	private static final String DOUBLE_REGEX = "\\-?\\s*\\d*\\.?\\d+";
	
	private static String regexArray(String compType) {
		return "\\s*((\\{\\s*\\})|(\\{\\s*" + compType + "\\s*+(,\\s*" + compType + "\\s*)*\\s*\\}))\\s*";
	}
	
	private static String regexIntArray = regexArray(INT_REGEX);
	private static String regexDoubleArray = regexArray(DOUBLE_REGEX);
	private static String regexBooleanArray = regexArray("(true|false)");
	private static String regexCharArray = regexArray("'.'");
	
//	private static String regexIntMatrix = "\\s*(\\{\\s*\\}\\s*)|(\\{" + regexIntArray + "(," + regexIntArray + ")*" + "\\})\\s*";
	
	private boolean validValue(String val, String pType, Combo combo) {
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
			
			else if(pType.equals(int.class.getName() + "[]")) return val.matches(regexIntArray);
			else if(pType.equals(double.class.getName() + "[]")) return val.matches(regexDoubleArray);
			else if(pType.equals(boolean.class.getName() + "[]")) return val.matches(regexBooleanArray);
			else if(pType.equals(char.class.getName() + "[]")) return val.matches(regexCharArray);
			
//			else if(pType.equals(int.class.getName() + "[][]")) return val.matches(regexIntMatrix);
			
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
			values[j] = trimZeros(paramBoxes[j].getText());
		return values;
	}
	


	private String trimZeros(String text) {
		if(text.startsWith("0") && text.length() > 1)
			return trimZeros(text.substring(1));
		else
			return text;
	}

	public String getInvocationExpression() {
		String[] values = getValues();
		String[] parameterTypes = method.getParameterTypes();

		for(int i = 0; i < values.length; i++) {
			String pType = Signature.getSignatureSimpleName(parameterTypes[i]);
			values[i] = convertForTypedInvocation(values[i], pType);
		}

		try {
			return (method.isConstructor() ? "new " + method.getElementName() : methodName) + "(" + String.join(", ", values) + ")";
		} catch (JavaModelException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String convertForTypedInvocation(String val, String pType) {
		
		if(pType.matches("byte|short|long"))
			return "(" + pType + ")" + val;
		if(pType.equals(float.class.getName()) && !val.endsWith("f"))
			return val + "f";
		else if(pType.equals(double.class.getName()) && val.indexOf('.') == -1)
			return val + ".0";
		
//		else if(pType.endsWith("[][]") && val.contains("{")) {
//			return "new " + pType + " " + val;
//		}
		else if(pType.endsWith("[]") && val.contains("{")) {
			if(val.matches("\\s*\\{\\s*\\}\\s*"))
				return "new " + pType.replace("[]", "[0]");
			else
				return "new " + pType + " " + val;
		}
		else
			return val;
	}


	@Override
	public boolean setFocus() {
		paramBoxes[0].setFocus();
		return true;
	}

	private String getMethodKey(IMethod method) {
		try {
			IType type = (IType) method.getParent();
			return type.getFullyQualifiedName() + "|" + method.getElementName() + method.getSignature();
		} catch (JavaModelException e) {
			e.printStackTrace();
			return null;
		}
	}

	void setCache(String[] values) {
		String key = getMethodKey(method);
		List<List<String>> comboValues = cache.get(key);
		if(comboValues == null) {
			comboValues = new ArrayList<>();
			for(int i = 0; i < method.getNumberOfParameters(); i++)
				comboValues.add(new ArrayList<>());
			cache.put(key, comboValues);
		}
		for(int i = 0; i < values.length; i++) {
			List<String> list = comboValues.get(i); 
			if(list.contains(values[i]))
				list.remove(values[i]);

			list.add(values[i]);
		}
	}
}