package pt.iscte.pandionj;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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

	private RuntimeModel model;
	private IStackFrame exceptionFrame;
	private String exception;

	private IDebugEventSetListener debugEventListener;
	private PandionJBreakpointListener breakpointListener;

	private ScrolledComposite scroll; 
	private Composite area;
	
	private StaticArea staticArea;
	private StackView stackView;
	
	private InvocationArea invocationArea;
	
	private Label labelInit;
	private StackLayout stackLayout;
	private IContextService contextService;

	private IToolBarManager toolBar;

	private Map<String, Image> images; // TODO image manager
	
	public PandionJView() {
		instance = this;
		images = new HashMap<>();
	}

	public static PandionJView getInstance() {
		return instance;
	}

	@Override
	public void createPartControl(Composite parent) {
		contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);

		createWidgets(parent);
		model = new RuntimeModel();
		stackView.setInput(model);
		
		debugEventListener = new DebugListener();
		DebugPlugin.getDefault().addDebugEventListener(debugEventListener);

		breakpointListener = new PandionJBreakpointListener();
		JDIDebugModel.addJavaBreakpointListener(breakpointListener);
		
		populateToolBar();
	}

	


	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(debugEventListener);
		JDIDebugModel.removeJavaBreakpointListener(breakpointListener);
		for(Image img : images.values())
			img.dispose();

		FontManager.dispose();
	}

	
	private void createWidgets(Composite parent) {
		stackLayout = new StackLayout();
		parent.setLayout(stackLayout);
		parent.setBackground(new Color(null, 255, 255, 255));

		scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		area = new Composite(scroll, SWT.NONE);
		area.setBackground(Constants.Colors.VIEW_BACKGROUND);

		scroll.setContent(area);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinHeight(100);
		scroll.setMinWidth(0);
		scroll.setAlwaysShowScrollBars(true);

		GridLayout layout = new GridLayout(1, true);
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.horizontalSpacing = 1;
		layout.verticalSpacing = 1;
		area.setLayout(layout);

		Composite labelComposite = new Composite(parent, SWT.NONE);
		labelComposite.setLayout(new GridLayout());
		labelInit = new Label(labelComposite, SWT.WRAP);
		FontManager.setFont(labelInit, Constants.MESSAGE_FONT_SIZE, Style.ITALIC);
		labelInit.setText(Constants.START_MESSAGE);
		labelInit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		stackLayout.topControl = labelComposite;

		staticArea = new StaticArea(area);
		staticArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		stackView = new StackView(area);
		stackView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		invocationArea = new InvocationArea(parent);
	}

	@Override
	public void setFocus() {
		scroll.setFocus();
		contextService.activateContext(Constants.CONTEXT_ID); // TODO constants
	}
	
	private class DebugListener implements IDebugEventSetListener {
		public void handleDebugEvents(DebugEvent[] events) {
			if(events.length > 0 && !model.isTerminated()) {
				DebugEvent e = events[0];
				if(e.getKind() == DebugEvent.SUSPEND && e.getDetail() == DebugEvent.STEP_END && exception == null) {
					IJavaThread thread = (IJavaThread) e.getSource();
					executeInternal(() -> {
						handleFrames(thread);
						if(thread.getTopStackFrame().getLineNumber() == -1)
							thread.resume();
					});
				}
//				else if(e.getKind() == DebugEvent.RESUME && e.getDetail() == DebugEvent.STEP_INTO) {
//					breakpointListener.enableFilter();
//				}
//				else if(e.getKind() == DebugEvent.RESUME &&
//						(
//						e.getDetail() == DebugEvent.STEP_OVER || 
//						e.getDetail() == DebugEvent.STEP_RETURN || 
//						e.getDetail() == DebugEvent.CLIENT_REQUEST 
//						))  {
//					breakpointListener.disableFilter();
//				}
				else if(e.getKind() == DebugEvent.TERMINATE) {
					model.setTerminated();
				}
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
			if(!model.isEmpty()) {
				StackFrameModel frame = model.getFrame(exceptionFrame);
				int line = exceptionFrame.getLineNumber();
				frame.processException(exception, line);  
			}
		}); 
	}


	private void handleFrames(IJavaThread thread) {
		assert thread != null;
		if(stackLayout.topControl != scroll) {
			Display.getDefault().syncExec(() -> {
				stackLayout.topControl = scroll;
				scroll.getParent().layout();
			});
		}
		
		model.update(thread);
		
		if(!model.isEmpty()) {
			StackFrameModel frame = model.getTopFrame();
			staticArea.setInput(frame);
			PandionJUI.navigateToLine(frame.getSourceFile(), frame.getLineNumber());
		}
	}


	public void executeInternal(PandionJUI.DebugRun r) {
		try {
			r.run();
		}
		catch(DebugException e) {
			model.setTerminated();
		}
	}

	public <T> T executeInternal(PandionJUI.DebugOperation<T> r, T defaultValue) {
		try {
			return r.run();
		}
		catch(DebugException e) {
			model.setTerminated();
			return null;
		}
	}

	
	public void promptInvocation(IMethod method, InvocationAction action) {
		stackLayout.topControl = invocationArea;
		invocationArea.setMethod(method, action);
		invocationArea.getParent().layout();
		invocationArea.setFocus();
	}


	
	
	
	
	
	
	private void populateToolBar() {
		toolBar = getViewSite().getActionBars().getToolBarManager();
		addToolbarAction("Run garbage collector", false, Constants.TRASH_ICON, Constants.TRASH_MESSAGE, () -> model.simulateGC());
		
		// TODO zoom all
		addToolbarAction("Zoom in", false, "zoomin.gif", null, () -> stackView.zoomIn());
		addToolbarAction("Zoom out", false, "zoomout.gif", null, () -> stackView.zoomOut());
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
				stackView.copyToClipBoard();
			}

			@Override
			public boolean isEnabled() {
				return !stackView.isEmpty();
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
		StackFrameModel stackFrame = model.getTopFrame();
		IWatchExpressionDelegate delegate = expressionManager.newWatchExpressionDelegate(stackFrame.getStackFrame().getModelIdentifier());	
		delegate.evaluateExpression(expression , stackFrame.getStackFrame(), new IWatchExpressionListener() {
			
			@Override
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				System.out.println("EVAL: " + result.getValue());
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
