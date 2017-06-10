package pt.iscte.pandionj;

import java.util.Arrays;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;

public class InvocationWidget extends Composite {
		private Combo[] textFields; 
		private String methodName;
		private String[] values;
		private IMethod method;
		
		public InvocationWidget(Composite parent, IMethod method, InvocationAction action) {
			super(parent, SWT.NONE);
			this.method = method;
			methodName = method.getElementName();
			
			setLayout(new RowLayout());
			org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(this, SWT.NONE);
			FontManager.setFont(label, Constants.MESSAGE_FONT_SIZE);

			label.setText(method.getElementName() + " (");

			textFields = new Combo[method.getNumberOfParameters()];
			for(int i = 0; i < method.getNumberOfParameters(); i++) {
				if(i != 0) {
					org.eclipse.swt.widgets.Label comma = new org.eclipse.swt.widgets.Label(this, SWT.NONE);
					FontManager.setFont(comma, Constants.BUTTON_FONT_SIZE);
					comma.setText(", ");
				}
				
				String pType = Signature.toString(method.getParameterTypes()[i]);
				Combo text = new Combo(this, SWT.DROP_DOWN);
				
				text.setToolTipText(pType);
				//				text.setLayoutData(new Row(40, 20));
				FontManager.setFont(text, Constants.MESSAGE_FONT_SIZE);
				textFields[i] = text;
				int ii = i;
				text.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent e) {
						text.setForeground(valid() ? null : Constants.ERROR_COLOR);
					}

					public void focusGained(FocusEvent e) {
						
					}

					private boolean valid() {
						String val = text.getText();
						try {
							if(pType.equals(String.class.getName())) return val.matches("\"(.)*\"|null");

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
				});
				text.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if(e.keyCode == SWT.CR) {
							if(ii == textFields.length-1) {
								values = new String[textFields.length];
								for(int j = 0; j < values.length; j++)
									values[j] = textFields[j].getText();

								action.invoke(getInvocationExpression());
							}
							else {
								textFields[ii+1].setFocus();
							}
						}
					}
				});
			}
			org.eclipse.swt.widgets.Label close = new org.eclipse.swt.widgets.Label(this, SWT.NONE);
			FontManager.setFont(close, Constants.MESSAGE_FONT_SIZE);
			close.setText(")");

			this.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.CR) {
						action.invoke(getInvocationExpression());
					}
				}
			});
		}



		
		
		public String[] getValues() {
			return Arrays.copyOf(values, values.length);
		}

		public String getInvocationExpression() {
			return methodName + "(" + String.join(",", values) + ")";
		}

		
		@Override
		public boolean setFocus() {
			textFields[0].setFocus();
			return true;
		}
	}