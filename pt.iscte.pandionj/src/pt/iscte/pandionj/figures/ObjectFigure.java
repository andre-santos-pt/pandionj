package pt.iscte.pandionj.figures;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.model.EntityModel;
import pt.iscte.pandionj.model.ObjectModel;

public class ObjectFigure extends RoundedRectangle {
	private Graph graph;
	private ObjectModel model;
	private Map<String, Label> fieldLabels;
	private Label headerLabel;

	public ObjectFigure(ObjectModel model, Graph graph, IFigure extensionFigure, boolean addMethods) {
		this.model = model;
		this.graph = graph;
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		setLayoutManager(layout);
		setCornerDimensions(new Dimension(10, 10));
		setLayoutManager(layout);
		setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		setBackgroundColor(Constants.OBJECT_COLOR);


		fieldLabels = new HashMap<String, Label>();

		if(extensionFigure == null) {
			headerLabel = new Label();
			headerLabel.setForegroundColor(Constants.OBJECT_HEADER_FONT_COLOR);
			FontManager.setFont(headerLabel, Constants.OBJECT_HEADER_FONT_SIZE);
			headerLabel.setText(model.toStringValue());
			add(headerLabel);
		}
		else
			add(extensionFigure);

		//		model.registerObserver(new Observer() {
		//			public void update(Observable o, Object arg) {
		//				Display.getDefault().asyncExec(() -> {
		//					headerLabel.setText(model.toStringValue());
		//				});
		//			}
		//		});

		try {
			setToolTip(new Label(model.getContent().getJavaType().getName()));
		} catch (DebugException e) {
			e.printStackTrace();
		}


		if(addMethods)
			addMethods(model, graph);

		setPreferredSize(getPreferredSize().expand(Constants.OBJECT_PADDING, Constants.OBJECT_PADDING));
	}


	private void addMethods(ObjectModel model, Graph graph) {
		for(IMethod m : model.getInstanceMethods()) {
			if(model.includeMethod(m)) {
				Figure methodFig = new Figure();
				methodFig.setLayoutManager(new FlowLayout());
				//			MethodInfo m = methods.next();


				Label but = new Label(m.getElementName() + (m.getNumberOfParameters() == 0 ? "()" : "(...)"));
				FontManager.setFont(but, Constants.BUTTON_FONT_SIZE);
				but.addMouseListener(new MouseListener() {

					@Override
					public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mousePressed(org.eclipse.draw2d.MouseEvent arg0) {
						Rectangle r = but.getBounds().getCopy();
						org.eclipse.swt.graphics.Point p = graph.toDisplay(r.x,r.y);
						IJavaValue ret = null;
						String args = "";
						if(m.getNumberOfParameters() == 0) {
							//							ret = model.invoke(m);
							model.invoke2(m, new IJavaValue[0], new IWatchExpressionListener() {

								@Override
								public void watchEvaluationFinished(IWatchExpressionResult result) {
									try {
										//										ret = (IJavaValue) result.getValue();
										System.out.println("FINISH EVAL! " + result.getValue().getValueString()); // TODO null
										
										processInvocationResult((IJavaValue) result.getValue()); 
									} catch (DebugException e) {
										e.printStackTrace();
									}
								}
							});	
						}
						else {
							ParamsDialog prompt = new ParamsDialog(Display.getDefault().getActiveShell(), p.x, p.y, m);
							if(prompt.open()) {
								IJavaDebugTarget debugTarget = (IJavaDebugTarget) model.getStackFrame().getStackFrame().getDebugTarget();
								IJavaValue[] values = null;
								try {
									values = createValues(m, prompt.values, debugTarget);
								} catch (DebugException e1) {
									e1.printStackTrace();
								}
								ret = model.invoke(m, values);
								args = String.join(", ", prompt.values);
							}
						}


						if(ret instanceof IJavaObject) {
							EntityModel<? extends IJavaObject> object = model.getStackFrame().getObject((IJavaObject) ret, true);
							System.out.println(ret + " == " + object);
						}
						else if(ret instanceof IJavaPrimitiveValue) {
							try {
								new ResultDialog(Display.getDefault().getActiveShell(), p.x, p.y, m.getElementName() + "(" + args + ") = " + ret.getValueString()).open();
							} catch (DebugException e1) {
								e1.printStackTrace();
							}
						}
						model.getStackFrame().update();
					}

					@Override
					public void mouseReleased(org.eclipse.draw2d.MouseEvent arg0) {
						// TODO Auto-generated method stub

					}

				});
				methodFig.add(but);
				add(methodFig);

				//			Button but = new Button(m.getName());
				//			FontManager.setFont(but, Constants.BUTTON_FONT_SIZE);
				//			but.addActionListener(new ActionListener() {
				//
				//				@Override
				//				public void actionPerformed(ActionEvent e) {
				//					Rectangle r = but.getBounds().getCopy();
				//					org.eclipse.swt.graphics.Point p = graph.toDisplay(r.x,r.y);
				//					IJavaValue ret = null;
				//					String args = "";
				//					if(m.getNumberOfParameters() == 0)
				//						ret = model.invoke(m);
				//					else {
				//						ParamsDialog prompt = new ParamsDialog(Display.getDefault().getActiveShell(), p.x + r.width, p.y, m);
				//						if(prompt.open()) {
				//							IJavaDebugTarget debugTarget = (IJavaDebugTarget) model.getStackFrame().getStackFrame().getDebugTarget();
				//							IJavaValue[] values = null;
				//							try {
				//								values = createValues(m, prompt.values, debugTarget);
				//							} catch (DebugException e1) {
				//								e1.printStackTrace();
				//							}
				//							ret = model.invoke(m, values);
				//							args = String.join(", ", prompt.values);
				//						}
				//					}
				//					if(ret instanceof IJavaObject)
				//						model.getStackFrame().getObject((IJavaObject) ret, true);
				//					else if(ret instanceof IJavaPrimitiveValue)
				//						try {
				//							new ResultDialog(Display.getDefault().getActiveShell(), p.x + r.width, p.y, "(" + args + ") = " + ret.getValueString()).open();
				//						} catch (DebugException e1) {
				//							e1.printStackTrace();
				//						}
				//				}
				//			});
			}
		}
	}

	private void processInvocationResult(IJavaValue ret) {
		if(ret instanceof IJavaObject) {
			EntityModel<? extends IJavaObject> object = model.getStackFrame().getObject((IJavaObject) ret, true);
			System.out.println(ret + " == " + object);
		}
//		else if(ret instanceof IJavaPrimitiveValue) {
//			try {
//				new ResultDialog(Display.getDefault().getActiveShell(), p.x, p.y, m.getElementName() + "(" + args + ") = " + ret.getValueString()).open();
//			} catch (DebugException e1) {
//				e1.printStackTrace();
//			}
//		}
		model.getStackFrame().update();
	}


	// TODO: move to Primitive type
	private IJavaValue[] createValues(IMethod m, String[] values, IJavaDebugTarget debugger) throws DebugException {
		assert values.length == m.getNumberOfParameters();

		IJavaValue[] v = new IJavaValue[values.length];
		for(int i = 0; i < v.length; i++) {
			String pType = Signature.toString(m.getParameterTypes()[i]);
			if(pType.equals(char.class.getName()))			v[i] = debugger.newValue(values[i].charAt(0));
			else if(pType.equals(boolean.class.getName())) 	v[i] = debugger.newValue(Boolean.parseBoolean(values[i]));
			else if(pType.equals(byte.class.getName())) 	v[i] = debugger.newValue(Byte.parseByte(values[i]));
			else if(pType.equals(short.class.getName()))	v[i] = debugger.newValue(Short.parseShort(values[i]));
			else if(pType.equals(int.class.getName())) 		v[i] = debugger.newValue(Integer.parseInt(values[i]));
			else if(pType.equals(long.class.getName())) 	v[i] = debugger.newValue(Long.parseLong(values[i]));
			else if(pType.equals(float.class.getName())) 	v[i] = debugger.newValue(Float.parseFloat(values[i]));
			else if(pType.equals(double.class.getName())) 	v[i] = debugger.newValue(Double.parseDouble(values[i]));

			else if(pType.equals(String.class.getName()) && values[i].matches("\"(.)*\"")) 	
				v[i] = debugger.newValue(values[i].substring(1, values[i].length()-1));

			else if(values[i].equals("null"))
				v[i] = debugger.nullValue();
			else {
				IJavaValue val = (IJavaValue) debugger.findVariable(values[i]);
				v[i] = val == null ? debugger.nullValue() : val;
			}
		}

		return v;

	}

	private class ParamsDialog {
		Shell shell;
		Text[] textFields;
		String[] values;
		public ParamsDialog(Shell parent, int x, int y, IMethod m) {
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
						text.setForeground(valid() ? null : Constants.ERROR_COLOR);
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
			shell.setLocation(x, y);
			shell.pack();
		}



		boolean open() {
			shell.open();
			while(!shell.isDisposed())
				if(!shell.getDisplay().readAndDispatch())
					shell.getDisplay().sleep();

			return values != null;
		}
	}

	private class ResultDialog {
		Shell shell;

		ResultDialog(Shell parent, int x, int y, String exp) {
			shell = new Shell(parent, SWT.PRIMARY_MODAL);
			shell.setLayout(new FillLayout());
			shell.setBackground(ColorConstants.white);
			org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(shell, SWT.BORDER);
			FontManager.setFont(label, Constants.BUTTON_FONT_SIZE);
			label.setText(exp);
			label.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					shell.close();
				}
			});
			shell.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.CR || e.keyCode == SWT.ESC) {
						shell.close();
					}
				}
			});

			shell.setLocation(x, y);
			shell.pack();
		}

		void open() {
			shell.open();
			while(!shell.isDisposed())
				if(!shell.getDisplay().readAndDispatch())
					shell.getDisplay().sleep();
		}

	}

}
