package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.GridData;
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
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel.Event;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVisibleMethod;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.ModelObserver;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.model.StackFrameModel;

public class ObjectFigure extends PandionJFigure<IObjectModel> {
	private RoundedRectangle fig;
	private StackContainer stack;
	private ObjectContainer objectContainer;
	private StackFrameFigure attributes;
	private Figure methodsFig;
	
	public ObjectFigure(IObjectModel model, IFigure extensionFigure, boolean addMethods) {
		super(model, true);
		assert extensionFigure != null;

		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 5;

		extensionFigure.setOpaque(true);

		fig = new RoundedRectangle();
		fig.setLayoutManager(layout);
		fig.setCornerDimensions(Constants.OBJECT_CORNER);
		fig.setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		fig.setBackgroundColor(Constants.Colors.OBJECT);
		getLayoutManager().setConstraint(fig, new GridData(SWT.DEFAULT, SWT.BEGINNING, false, false));

		fig.add(extensionFigure);
		fig.setToolTip(new Label(model.getTypeName()));

		RuntimeViewer runtimeViewer = RuntimeViewer.getInstance();
		objectContainer = new ObjectContainer(false);
		objectContainer.setFigProvider(runtimeViewer.getFigureProvider());

		addShowMethodListener();
		
		model.getRuntimeModel().registerDisplayObserver(new ModelObserver<IRuntimeModel.Event<?>>() {
			
			@Override
			public void update(Event<?> e) {
				if(e.type == IRuntimeModel.Event.Type.REMOVE_FRAME) {
					IStackFrameModel f = (IStackFrameModel) e.arg;
					stack.removeFrame(f);
				}
				else if(e.type == RuntimeModel.Event.Type.NEW_FRAME) {
					StackFrameModel f = (StackFrameModel) e.arg;
					if(f.getThis() == getModel())
						addFrame(f);
				}
				else if(e.type == IRuntimeModel.Event.Type.STEP) {
					IStackFrameModel f = model.getRuntimeModel().getTopFrame();
					if(!(f.isInstance() && f.getThis() == getModel()) && attributes != null) {
//						stack.removePointers();
//						stack.removeAll();
//						objectContainer.removeAll();
//						if(attributes != null)
//							attributes.setVisible(false);
//						attributes = null;
						runtimeViewer.updateLayout();
					}
				}
			}
		});

//		if(addMethods)
//			addMethods(model);
		
		add(fig);
		add(objectContainer);

		stack = new StackContainer();
		fig.add(stack);
		layout.setConstraint(stack, new GridData(SWT.FILL, SWT.DEFAULT, true, false));

	}


	private void addShowMethodListener() {
		fig.addMouseListener(new MouseListener() {
			public void mouseReleased(org.eclipse.draw2d.MouseEvent me) { }
			public void mousePressed(org.eclipse.draw2d.MouseEvent me) { }
			public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent me) {
				if(methodsFig == null)
					addMethods(getModel());
				else {
					fig.remove(methodsFig);
					methodsFig = null;
				}
				invalidate();
			}
		});
	}
	

	public void addFrame(IStackFrameModel frame) {

		if(attributes == null) {
			attributes = new StackFrameFigure(RuntimeViewer.getInstance(), frame, objectContainer, true, true);
			stack.add(attributes);
			stack.getLayoutManager().setConstraint(attributes, new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));

		}

		stack.addFrame(frame, RuntimeViewer.getInstance(), objectContainer, false);
	}	



	private void addMethods(IObjectModel model) {
		methodsFig = new Figure();
		methodsFig.setLayoutManager(new GridLayout(1, false));
//		methodsFig.setLayoutManager(new FlowLayout());
		for(IVisibleMethod m : model.getVisibleMethods()) {
			
			Label but = new Label(m.getName() + (m.getNumberOfParameters() == 0 ? "()" : "(...)"));
			FontManager.setFont(but, Constants.BUTTON_FONT_SIZE);
			Button button = new Button(m.getName() + (m.getNumberOfParameters() == 0 ? "()" : "(...)"));
			button.setToolTip(new Label("click to invoke method"));
			button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent event) {
					invoke(model, m);
				}
			});
			
			methodsFig.add(button);
			but.addMouseListener(new MouseListener() {

				@Override
				public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent arg0) {
				}

				@Override
				public void mousePressed(org.eclipse.draw2d.MouseEvent arg0) {
					invoke(model, m);
				}

				@Override
				public void mouseReleased(org.eclipse.draw2d.MouseEvent arg0) {

				}
			});
//			methodFig.add(but);
		}
		fig.add(methodsFig);
	}


	private void invoke(IObjectModel model, IVisibleMethod m) {
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
}
