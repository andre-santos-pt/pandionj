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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;
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
import pt.iscte.pandionj.extensibility.PandionJUI;
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

	private IMethod method;
	private InvokeDialog invokeDialog;
	private Combo[] paramBoxes; 
	private String[] parameterTypes;

	public StaticInvocationWidget(Composite parent, InvokeDialog invokeDialog, IMethod method) {
		super(parent, SWT.NONE);
		this.invokeDialog = invokeDialog;
		
		this.method = method;

		setLayout(rowLayout);
		
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
		invokeDialog.setValid(allvalid, allvalid ? PandionJUI.generateInvocationScript(method, getInvocationExpression(), getValues()) : null, getValues(), getExpressionValues());
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
		return validValue(combo.getText(), pType) ||  containsItem(combo, combo.getText());
	}

	public static String baseType(String pType) {
			return pType.substring(0, pType.indexOf('['));
	}
	
	public static boolean isArrayType1D(String pType) {
		return pType.endsWith("[]") && !pType.endsWith("[][]");
	}
	
	public static boolean isArrayType2D(String pType) {
		return pType.endsWith("[][]") && !pType.endsWith("[][][]");
	}
	
	public static boolean isPrimitive(String pType) {
		return pType.matches("boolean|char|byte|short|int|long|double|float");
	}
	
	private boolean allSameType(ArrayInitializer init, String pType, boolean dim2) {
		for(Object o : init.expressions()) {
			Expression e = (Expression) o;
			if(e instanceof ArrayInitializer) {
				boolean tmp = allSameType((ArrayInitializer) e, pType, false);
				if(!tmp)
					return false;
			}
			else if(e instanceof NullLiteral)  {
				if(!dim2)
					return false;
			}
			else {
				if(dim2)
					return false;
			String val = e.toString();
			try {
				if(pType.equals(String.class.getSimpleName()) && !val.matches("(\"(.)*\")|null")) return false;
				else if(pType.equals(char.class.getName()) && !val.matches("'.'")) return false;
				else if(pType.equals(boolean.class.getName()) && !val.matches("true|false")) return false;
				else if(pType.equals(byte.class.getName())) Byte.parseByte(val);
				else if(pType.equals(short.class.getName())) Short.parseShort(val);
				else if(pType.equals(int.class.getName())) Integer.parseInt(val);
				else if(pType.equals(long.class.getName())) Long.parseLong(val);
				else if(pType.equals(float.class.getName())) Float.parseFloat(val);
				else if(pType.equals(double.class.getName())) Double.parseDouble(val);
			}
				catch (Exception e2) {
					return false;
				}
			}
		}
			
		return true;
	}
	
	private boolean validValue(String val, String pType) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
			
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setSource(val.toCharArray());
		ASTNode exp = parser.createAST(null);
	
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
			
			else if(exp instanceof ArrayInitializer && isArrayType1D(pType)) {
				String baseType = baseType(pType);
				return allSameType((ArrayInitializer) exp, baseType, false);
			}
			
			else if(exp instanceof ArrayInitializer && isArrayType2D(pType)) {
				String baseType = baseType(pType);
				return allSameType((ArrayInitializer) exp, baseType, true);
			}
//			else if(exp instanceof ClassInstanceCreation) {
//				ClassInstanceCreation newInst = (ClassInstanceCreation) exp;
//				//method.getClassFile().get
//				
//			}
//			else if(exp instanceof MethodInvocation) {
//				
//				System.out.println(exp);
//				
//			}
			else 
				return false;
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


	private String[] getValues() {
		String[] values = new String[paramBoxes.length];
		for(int j = 0; j < values.length; j++) {
			values[j] = trimZeros(paramBoxes[j].getText());
		}
		return values;
	}
	
	private String trimZeros(String text) {
		if(text.startsWith("0") && text.length() > 1)
			return trimZeros(text.substring(1));
		else
			return text;
	}
	
	private String[] getExpressionValues() {
		String[] parameterTypes = method.getParameterTypes();
		String[] values = getValues();

		for(int i = 0; i < values.length; i++) {
			String pType = Signature.getSignatureSimpleName(parameterTypes[i]);
			values[i] = convertForTypedInvocation(values[i], pType);
		}
		return values;
	}

	private String convertForTypedInvocation(String val, String pType) {
		
		if(pType.matches("byte|short|long"))
			return "(" + pType + ")" + val;
		if(pType.equals(float.class.getName()) && !val.endsWith("f"))
			return val + "f";
		else if(pType.equals(double.class.getName()) && val.indexOf('.') == -1)
			return val + ".0";
		
		else if(pType.endsWith("[]") && val.contains("{")) {
			if(val.matches("\\s*\\{\\s*\\}\\s*"))
				return "new " + pType.replace("[]", "[0]");
			else
				return "new " + pType + " " + val;
		}
		else
			return val;
	}
	
	public String getInvocationExpression() {
		String[] values = getExpressionValues();

		try {
			return (method.isConstructor() ? "new " + method.getElementName() : method.getElementName()) + "(" + String.join(", ", values) + ")";
		} catch (JavaModelException e) {
			e.printStackTrace();
			return null;
		}
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