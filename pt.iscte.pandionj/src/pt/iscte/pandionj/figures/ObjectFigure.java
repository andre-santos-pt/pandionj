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
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.ParamsDialog;
import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;
import pt.iscte.pandionj.extensibility.IVisibleMethod;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.ObjectModel;

public class ObjectFigure extends PandionJFigure<IObjectModel> {
	private Map<String, Label> fieldLabels;
	private Label headerLabel;
	private RoundedRectangle fig;

	public ObjectFigure(IObjectModel model, IFigure extensionFigure, boolean addMethods) {
		super(model, true);
		assert extensionFigure != null;
		
		fieldLabels = new HashMap<String, Label>();
		
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		
		extensionFigure.setOpaque(true);
		extensionFigure.setBackgroundColor(ColorConstants.blue);
		
		fig = new RoundedRectangle();
		fig.setLayoutManager(layout);
		fig.setCornerDimensions(Constants.OBJECT_CORNER);
		fig.setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		fig.setBackgroundColor(Constants.Colors.OBJECT);

		fig.add(extensionFigure);
		fig.setToolTip(new Label(model.getTypeName()));

		RuntimeViewer runtimeViewer = RuntimeViewer.getInstance();
		ObjectContainer objectContainer = new ObjectContainer(false);
		objectContainer.setFigProvider(runtimeViewer.getFigureProvider());
//		objectContainer.setBackgroundColor(ColorConstants.yellow);
		
		StackFrameFigure sf = new StackFrameFigure(runtimeViewer, model.getRuntimeModel().getTopFrame(), objectContainer, true, true);
		fig.add(sf);

		if(addMethods)
			addMethods(model);
		
		add(fig);
		add(objectContainer);
	
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
			Figure methodFig = new Figure();
			methodFig.setLayoutManager(new FlowLayout());

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
						model.invoke(m.getName(), new InvocationResult() {

							@Override
							public void valueReturn(Object o) {
								System.out.println("CLICK: " + o);
							}
						});
					}
					else {
						ParamsDialog prompt = new ParamsDialog(Display.getDefault().getActiveShell(), m);
						prompt.setLocation(p.x, p.y);
						if(prompt.open()) {
							String[] stringValues = prompt.getValues();
							model.invoke(m.getName(), new InvocationResult() {
								@Override
								public void valueReturn(Object o) {
									PandionJUI.executeUpdate(() -> {
										new ResultDialog(Display.getDefault().getActiveShell(), 400, 400, o.toString()).open();
									});
								}
							}, stringValues);
						}
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
