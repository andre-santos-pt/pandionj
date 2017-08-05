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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ParamsDialog {
		private Shell shell;
		private Text[] textFields; 
		private String methodName;
		private String[] values;
		
		public ParamsDialog(Shell parent, IMethod m) {
			methodName = m.getElementName();
			shell = new Shell(parent, SWT.PRIMARY_MODAL);
			shell.setText(m.getElementName());
			//			org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(m.getNumberOfParameters(), true);
			//			layout.marginLeft = 0;
			//			layout.marginTop = 0;
			//			layout.horizontalSpacing = 0;
			//			layout.verticalSpacing = 0;

			shell.setLayout(new RowLayout());
			org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(shell, SWT.NONE);
			label.setText(m.getElementName() + " (");
			FontManager.setFont(label, Constants.BUTTON_FONT_SIZE);

			textFields = new Text[m.getNumberOfParameters()];
			for(int i = 0; i < m.getNumberOfParameters(); i++) {
				if(i != 0) {
					org.eclipse.swt.widgets.Label comma = new org.eclipse.swt.widgets.Label(shell, SWT.NONE);
					FontManager.setFont(comma, Constants.BUTTON_FONT_SIZE);
					comma.setText(", ");
				}
				;
				String pType = Signature.toString(m.getParameterTypes()[i]);
				Text text = new org.eclipse.swt.widgets.Text(shell, SWT.BORDER);
				text.setToolTipText(pType);
				//				text.setLayoutData(new Row(40, 20));
				FontManager.setFont(text, Constants.BUTTON_FONT_SIZE);
				textFields[i] = text;
				int ii = i;
				text.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent e) {
						text.setForeground(valid() ? null : Constants.Colors.ERROR);
					}

					public void focusGained(FocusEvent e) {
						text.selectAll();
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

								shell.close();
							}
							else {
								textFields[ii+1].setFocus();
							}
							//shell.close();
						}

					}


				});
			}
			org.eclipse.swt.widgets.Label close = new org.eclipse.swt.widgets.Label(shell, SWT.NONE);
			FontManager.setFont(close, Constants.BUTTON_FONT_SIZE);
			close.setText(")");

			shell.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.CR) {
						shell.close();
					}
				}
			});
			shell.pack();
		}



		public boolean open() {
			shell.open();
			while(!shell.isDisposed())
				if(!shell.getDisplay().readAndDispatch())
					shell.getDisplay().sleep();

			return values != null;
		}
		
		public String[] getValues() {
			return Arrays.copyOf(values, values.length);
		}

		public String getInvocationExpression() {
			return methodName + "(" + String.join(",", values) + ")";
		}

		
		public void setLocation(int x, int y) {
			shell.setLocation(x, y);
		}
	}