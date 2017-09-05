package pt.iscte.pandionj;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

import pt.iscte.pandionj.FontManager.Style;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.model.StackFrameModel;

public class PandionJView extends ViewPart { 
	private static PandionJView instance;

	private RuntimeModel runtime;
	private IStackFrame exceptionFrame;
	private String exception;

	private IDebugEventSetListener debugEventListener;
	private PandionJBreakpointListener breakpointListener;

	private StackLayout stackLayout;
	private Label labelInit;
	private InvocationArea invocationArea;
	private RuntimeViewer runtimeView;

	private IContextService contextService;

	private IToolBarManager toolBar;

	public PandionJView() {
		instance = this;
	}

	public static PandionJView getInstance() {
		return instance;
	}

	@Override
	public void createPartControl(Composite parent) {
		contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		createWidgets(parent);

		debugEventListener = new DebugListener();
		DebugPlugin.getDefault().addDebugEventListener(debugEventListener);

		breakpointListener = new PandionJBreakpointListener();
		JDIDebugModel.addJavaBreakpointListener(breakpointListener);

		//		populateToolBar();
	}


	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(debugEventListener);
		JDIDebugModel.removeJavaBreakpointListener(breakpointListener);
		FontManager.dispose();
		instance = null;
	}


	private void createWidgets(Composite parent) {
		stackLayout = new StackLayout();
		stackLayout.marginHeight = 0;
		stackLayout.marginWidth = 0;
		parent.setLayout(stackLayout);
		parent.setBackground(Constants.Colors.VIEW_BACKGROUND);

		Composite labelComposite = new Composite(parent, SWT.BORDER);
		labelComposite.setLayout(new GridLayout());
		labelInit = new Label(labelComposite, SWT.WRAP);
		FontManager.setFont(labelInit, Constants.MESSAGE_FONT_SIZE, Style.ITALIC);
		labelInit.setText(Constants.Messages.START);
		labelInit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		stackLayout.topControl = labelComposite;

		runtimeView = new RuntimeViewer(parent);
		runtimeView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		invocationArea = new InvocationArea(parent);
		setTitleToolTip(getVersion());
	}

	static String getVersion() {
		return "Version " + Platform.getBundle(Constants.PLUGIN_ID).getVersion().toString();
	}

	@Override
	public void setFocus() {
		//		area.setFocus();
		contextService.activateContext(Constants.CONTEXT_ID);
	}



	private class DebugListener implements IDebugEventSetListener {
		public void handleDebugEvents(DebugEvent[] events) {
			if(events.length > 0 && runtime != null && !runtime.isTerminated()) {
				DebugEvent e = events[0];
				if(e.getKind() == DebugEvent.SUSPEND && e.getDetail() == DebugEvent.STEP_END && exception == null) {
					IJavaThread thread = (IJavaThread) e.getSource();
					executeInternal(() -> {
						handleFrames(thread);
						IStackFrame f = thread.getTopStackFrame();
						if(f != null && f.getLineNumber() == -1)
							thread.resume();
					});
				}
				else if(e.getKind() == DebugEvent.TERMINATE) {
					runtime.setTerminated();
				}
				// TODO: STEP RETURN -> remove frame 
			}
		}
	}


	void handleLinebreakPoint(IJavaThread thread) {
		executeInternal(() -> {
			exception = null;
			exceptionFrame = null;
			handleFrames(thread);
		});
	}

	void handleExceptionBreakpoint(IJavaThread thread, IJavaExceptionBreakpoint exceptionBreakPoint) {
		executeInternal(() -> {
			thread.terminateEvaluation();
			exception = exceptionBreakPoint.getExceptionTypeName();
			exceptionFrame = thread.getTopStackFrame();
			handleFrames(thread);
			if(!runtime.isEmpty()) {
				StackFrameModel frame = runtime.getFrame(exceptionFrame);
				int line = exceptionFrame.getLineNumber();
				frame.processException(exception, line);  
			}
		}); 
	}


	private void handleFrames(IJavaThread thread) {
		assert thread != null;
		if(stackLayout.topControl != runtimeView) {
			Display.getDefault().syncExec(() -> {
				stackLayout.topControl = runtimeView;
				runtimeView.requestLayout();
			});
		}

		executeInternal(() -> {
			if(runtime == null || !runtime.isPartiallyCommon(thread.getStackFrames())) {
				runtime = new RuntimeModel();
				runtimeView.setInput(runtime);
			}

			runtime.update(thread);

			if(!runtime.isEmpty() && !runtime.isTerminated()) {
				StackFrameModel frame = runtime.getTopFrame();
				IFile sourceFile = frame.getSourceFile();
				int lineNumber = frame.getLineNumber();
				if(sourceFile != null && lineNumber != -1)
					PandionJUI.navigateToLine(sourceFile, lineNumber);
			}
		});
	}


	public void executeInternal(PandionJUI.DebugRun r) {
		try {
			r.run();
		}
		catch(DebugException e) {
			runtime.setTerminated();
		}
	}

	public <T> T executeInternal(PandionJUI.DebugOperation<T> r, T defaultValue) {
		try {
			return r.run();
		}
		catch(DebugException e) {
			runtime.setTerminated();
			return null;
		}
	}


	public void promptInvocation(IMethod method, InvocationAction action) {
		stackLayout.topControl = invocationArea;
		invocationArea.setMethod(method, action);
		//		invocationArea.requestLayout();
		invocationArea.getParent().layout();
		invocationArea.setFocus();
	}








	private void populateToolBar() {
		toolBar = getViewSite().getActionBars().getToolBarManager();
		addToolbarAction("Run garbage collector", false, Constants.TRASH_ICON, Constants.Messages.TRASH, () -> runtime.simulateGC());

		//		addToolbarAction("Zoom in", false, "zoomin.gif", null, () -> stackView.zoomIn());
		//		addToolbarAction("Zoom out", false, "zoomout.gif", null, () -> stackView.zoomOut());
		//		addToolbarAction("Highlight", true, "highlight.gif", "Activates the highlight mode, which ...", () -> {});
		//		addToolbarAction("Clipboard", false, "clipboard.gif", "Copies the visible area of the top frame as image to the clipboard.", () -> stackView.copyToClipBoard());
		addMenuBarItems();
	}


	private void addMenuBarItems() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		menuManager.add(new Action("highlight color") {
		});
		menuManager.add(new Action("Copy canvas to clipboard") {
			@Override
			public void run() {
				//				stackView.copyToClipBoard();
			}

			@Override
			public boolean isEnabled() {
				//				return !stackView.isEmpty();
				return false;
			}
		});
	}

	private void addToolbarAction(String name, boolean toggle, String imageName, String description, Action action) {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		action.setImageDescriptor(ImageDescriptor.createFromImage(PandionJUI.getImage(imageName)));
		String tooltip = name;
		if(description != null)
			tooltip += "\n" + description;
		action.setToolTipText(tooltip);
		menuManager.add(action);
	}

	private void addToolbarAction(String name, boolean toggle, String imageName, String description, Runnable runnable) {
		Action a = new Action(name, toggle ? Action.AS_CHECK_BOX : Action.AS_PUSH_BUTTON) {
			public void run() {
				runnable.run();
			}
		};
		addToolbarAction(name, toggle, imageName, description, a);
	}


	public void evaluate(String expression, InvocationResult listener) {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		StackFrameModel stackFrame = runtime.getTopFrame();

		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(stackFrame.getStackFrame().getModelIdentifier());	
		delegate.evaluateExpression(expression , stackFrame.getStackFrame(), new IWatchExpressionListener() {

			@Override
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				listener.valueReturn(result.getValue());
				System.out.println("? " + result.getValue());
			}
		});

	}



	//	private IDebugContextListener debugUiListener;
	//	debugUiListener = new DebugUIListener();
	//	DebugUITools.getDebugContextManager().addDebugContextListener(debugUiListener);
	//	DebugUITools.getDebugContextManager().removeDebugContextListener(debugUiListener);


	//	private class DebugUIListener implements IDebugContextListener {
	//		public void debugContextChanged(DebugContextEvent event) {
	//			IStackFrame f = getSelectedFrame(event.getContext());
	//			if(f != null && (event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
	//				openExpandItem(f);
	//			}
	//		}
	//
	//		private void openExpandItem(IStackFrame f) {
	//			for(ExpandItem e : callStack.getItems())
	//				e.setExpanded(((StackView) e.getControl()).model.getStackFrame() == f);
	//		}
	//
	//		private IStackFrame getSelectedFrame(ISelection context) {
	//			if (context instanceof IStructuredSelection) {
	//				Object data = ((IStructuredSelection) context).getFirstElement();
	//				if (data instanceof IStackFrame)
	//					return (IStackFrame) data;
	//			}
	//			return null;
	//		}
	//	}


}
