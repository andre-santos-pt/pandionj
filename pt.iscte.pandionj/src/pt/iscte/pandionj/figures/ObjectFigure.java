package pt.iscte.pandionj.figures;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;
import pt.iscte.pandionj.extensibility.IVisibleMethod;

public class ObjectFigure extends PandionJFigure<IObjectModel> {
//	private IObjectModel model;
	private Map<String, Label> fieldLabels;
	private Label headerLabel;
	private RoundedRectangle fig;
	
	public ObjectFigure(IObjectModel model, IFigure extensionFigure, boolean addMethods) {
		super(model, true);
		assert extensionFigure != null;
//		this.model = model;
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		fig = new RoundedRectangle();
		fig.setLayoutManager(layout);
		fig.setCornerDimensions(new Dimension(10, 10));
		fig.setLayoutManager(layout);
		fig.setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		fig.setBackgroundColor(Constants.Colors.OBJECT);


		fieldLabels = new HashMap<String, Label>();
		fig.add(extensionFigure);

		fig.setToolTip(new Label(model.getTypeName()));

		if(addMethods)
			addMethods(model);

		add(fig);
//		setPreferredSize(fig.getPreferredSize().expand(Constants.OBJECT_PADDING, Constants.OBJECT_PADDING));
	}


	//	private IWatchExpressionListener invocationListener = new IWatchExpressionListener() {
	//
	//		@Override
	//		public void watchEvaluationFinished(IWatchExpressionResult result) {
	//			//			try {
	//			IJavaValue ret = (IJavaValue) result.getValue();
	//			if(ret != null) {
	//				processInvocationResult((IJavaValue) result.getValue()); 
	//			}
	//			else {
	//				DebugException exception = result.getException();
	//				if(exception.getCause() instanceof InvocationException) {
	//					InvocationException e = (InvocationException) exception.getCause();
	//					ObjectReference exception2 = e.exception();
	//				}
	//			}
	//			//			} catch (DebugException e) {
	//			//				e.printStackTrace();
	//			//			}
	//		}
	//
	//		private void processInvocationResult(IJavaValue ret) {
	//			if(ret instanceof IJavaObject) {
	//				EntityModel<? extends IJavaObject> object = model.getRuntimeModel().getObject((IJavaObject) ret, true);
	//				System.out.println(ret + " == " + object);
	//			}
	//			else if(ret instanceof IJavaPrimitiveValue) {
	//				PandionJUI.executeUpdate(() -> {
	//					try {
	//						//					new ResultDialog(Display.getDefault().getActiveShell(), p.x, p.y, m.getElementName() + "(" + args + ") = " + ret.getValueString()).open();
	//						new ResultDialog(Display.getDefault().getActiveShell(), 400, 400, ret.getValueString()).open();
	//					} catch (DebugException e) {
	//						e.printStackTrace();
	//					}
	//				});
	//			}
	//			model.getRuntimeModel().refresh();
	//		}
	//	};


	private void addMethods(IObjectModel model) {
		for(IVisibleMethod m : model.getVisibleMethods()) {
			//			if(model.includeMethod(m)) {
			Figure methodFig = new Figure();
			methodFig.setLayoutManager(new FlowLayout());
			//			MethodInfo m = methods.next();


			Label but = new Label(m.getName() + (m.getNumberOfParameters() == 0 ? "()" : "(...)"));
			FontManager.setFont(but, Constants.BUTTON_FONT_SIZE);

			but.addMouseListener(new MouseListener() {

				@Override
				public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent arg0) {

				}

				@Override
				public void mousePressed(org.eclipse.draw2d.MouseEvent arg0) {
					Rectangle r = but.getBounds().getCopy();
					//						org.eclipse.swt.graphics.Point p = graph.toDisplay(r.x,r.y);
					org.eclipse.swt.graphics.Point p = new org.eclipse.swt.graphics.Point (400, 400);
					if(m.getNumberOfParameters() == 0) {
						model.invoke(m.getName(), new InvocationResult() {});
					}
					else {
						//							ParamsDialog prompt = new ParamsDialog(Display.getDefault().getActiveShell(), m);
						//							prompt.setLocation(p.x, p.y);
						//							if(prompt.open()) {
						//								//								IJavaDebugTarget debugTarget = (IJavaDebugTarget) model.getRuntimeModel().getTopFrame().getStackFrame().getDebugTarget();
						//								//								IJavaValue[] values = null;
						//								String[] stringValues = prompt.getValues();
						//								//								try {
						//								//									values = createValues(m, stringValues, debugTarget);
						//								//								} catch (DebugException e1) {
						//								//									e1.printStackTrace();
						//								//								}
						//								model.invoke(m.getElementName(), new InvocationResult() {
						//									@Override
						//									public void valueReturn(Object o) {
						//										PandionJUI.executeUpdate(() -> {
						//											try {
						//												new ResultDialog(Display.getDefault().getActiveShell(), 400, 400, o.toString()).open();
						//											} catch (DebugException e) {
						//												e.printStackTrace();
						//											}
						//										});
						//									}
						//								}, stringValues);
						//							}
					}
				}

				@Override
				public void mouseReleased(org.eclipse.draw2d.MouseEvent arg0) {

				}
			});
			methodFig.add(but);
			fig.add(methodFig);
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
