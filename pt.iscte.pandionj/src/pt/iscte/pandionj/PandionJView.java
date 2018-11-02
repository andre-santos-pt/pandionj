package pt.iscte.pandionj;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIReturnValueVariable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.model.StackFrameModel;

public class PandionJView extends ViewPart { 
	private static PandionJView instance;

	private ILaunch launch; 
	private RuntimeModel runtime;
	private IStackFrame exceptionFrame;
	private String exception;

	private IDebugEventSetListener debugEventListener;
	private PandionJBreakpointListener breakpointListener;
	private ErrorHandler logListener;

	private RuntimeViewer runtimeView;
	private Composite parent;

	private IContextService contextService;

	private IToolBarManager toolBar;

	private static int arrayMax = 10;

	private Composite introScreen;

	public PandionJView() {
		instance = this;
	}

	public static PandionJView getInstance() {
		return instance;
	}

	public static int getMaxArrayLength() {
		return arrayMax;
	}

	public static void setArrayMaximumLength(int val) {
		arrayMax = val;
	}

	@Override
	public void createPartControl(Composite parent) {
		contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		createWidgets(parent);

		debugEventListener = new DebugListener();
		DebugPlugin.getDefault().addDebugEventListener(debugEventListener);

		breakpointListener = new PandionJBreakpointListener();
		JDIDebugModel.addJavaBreakpointListener(breakpointListener);

		logListener = new ErrorHandler(this);
		Platform.addLogListener(logListener);
	}



	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(debugEventListener);
		JDIDebugModel.removeJavaBreakpointListener(breakpointListener);
		FontManager.dispose();
		instance = null;
	}

	RuntimeModel getRuntime() {
		return runtime;
	}

	private void createWidgets(Composite parent) {
		this.parent = parent;
		String toolTipVersion = "Version " + Platform.getBundle(PandionJConstants.PLUGIN_ID).getVersion().toString();
		setTitleToolTip(toolTipVersion);
		parent.setLayout(new GridLayout());
		introScreen = createIntroScreen(parent);
	}

	private Composite createIntroScreen(Composite parent) {
		Composite introComp = new Composite(parent, SWT.NONE);
		introComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		introComp.setLayout(new GridLayout());

		Image image = PandionJUI.getImage("pandionj.png");
		Label imageLabel = new Label(introComp, SWT.NONE);
		imageLabel.setImage(image);
		imageLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		Label versionLabel = new Label(introComp, SWT.NONE);
		versionLabel.setText(getTitleToolTip());
		versionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false));

		Link pluginLabel = new Link(introComp, SWT.NONE);
		pluginLabel.setText("<a>" + PandionJConstants.Messages.INSTALLED_TAGS + "</a>");
		pluginLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false));
		pluginLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String info = "";
				for (ExtensionManager.TagDescription desc : ExtensionManager.getTagDescriptions()) {
					String where = desc.description;
					if(where == null)
						where = "";
					else
						where += "\n";
					info += "@" + desc.tag + "\n" + where + "\n";
				}
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Installed tags", info);
			}
		});

		return introComp;
	}


	public void setFocus() {
	}




	private class DebugListener implements IDebugEventSetListener {
		public void handleDebugEvents(DebugEvent[] events) {
			if(events.length > 0 && runtime != null && !runtime.isTerminated()) {
				DebugEvent e = events[0];
				PandionJUI.executeUpdate(() -> {
					if(e.getKind() == DebugEvent.SUSPEND && e.getDetail() == DebugEvent.STEP_END && exception == null) {
						IJavaThread thread = (IJavaThread) e.getSource();		
						IStackFrame f = thread.getTopStackFrame();
						if(f == null)
							return;
						ISourceLocator sourceLocator = f.getLaunch().getSourceLocator();
						Object sourceElement = sourceLocator == null ? null : sourceLocator.getSourceElement(f);

						if(sourceElement != null) {
							if(sourceElement instanceof IFile)
								handleFrames(thread);
							else
								thread.stepReturn();

							if(f != null && f.getLineNumber() == -1 || thread.isSystemThread() || thread.isDaemon())
								thread.resume(); // to jump over injected code
						}
						else {
							thread.stepReturn();
						}
					}
					// TODO repor? -- ao repor faz com que os extension widgets nao aparecam ao suspender
					//					else if(e.getKind() == DebugEvent.CHANGE && e.getDetail() == DebugEvent.CONTENT) {
					//						runtime = new RuntimeModel();
					//						runtimeView.setInput(runtime);
					//					}
					else if(e.getKind() == DebugEvent.TERMINATE && e.getSource() instanceof RuntimeProcess) {
						runtime.setTerminated();
					}
				});
			}
		}
	}


	void handleLinebreakPoint(IJavaThread thread) {
		PandionJUI.executeUpdate(() -> {
			exception = null;
			exceptionFrame = null;
			handleFrames(thread);
		});
	}

	public void handleExceptionBreakpoint(IJavaThread thread, String exceptionName) {
		PandionJUI.executeUpdate(() -> {
			thread.terminateEvaluation();
			exception = exceptionName;
			exceptionFrame = thread.getTopStackFrame();
			handleFrames(thread);
			if(!runtime.isEmpty()) {
				StackFrameModel frame = runtime.getFrame(exceptionFrame);
				Exc exc = findException(exceptionFrame);
				if(exc == null)
					return;
				String message = exc.message;
				String dialogTitle = "Exception Raised: " + PandionJConstants.Messages.prettyException(exc.typeName);
				String dialogText = "null".equals(message) ? "" : message;
				if(exc.matches(ArrayIndexOutOfBoundsException.class)) {
					dialogText = "Array was accessed at the invalid index " + message + ".";
				}
				else if(exc.matches(NullPointerException.class)) {
					dialogText = "No object can be accessed through a null reference.";
				}
				else if(exc.matches(AssertionError.class)) {
					dialogText = "null".equals(message) ? "Assertion check failed." : message;
				}
				else if(exc.matches(NegativeArraySizeException.class)) {
					dialogText = "A negative value cannot be used to provide the array size.";
				}
				
				int line = frame.getLineNumber();
				frame.processException(exception, line, message);

				MessageDialog.open(MessageDialog.ERROR, Display.getDefault().getActiveShell(), 
						dialogTitle, dialogText, SWT.NONE);
				thread.resume();
			}
		}); 
	}

	private static class Exc {
		final String typeName;
		final String message;

		public Exc(String typeName, String message) {
			this.typeName = typeName;
			this.message = message;
		}
		
		boolean matches(Class<?> c) {
			return c.getName().equals(typeName);
		}
	}

	private static Exc findException(IStackFrame frame) throws DebugException {
		for(IVariable var : frame.getVariables()) {
			if(var instanceof JDIReturnValueVariable) {
				JDIReturnValueVariable retvar = (JDIReturnValueVariable) var;
				if(retvar.hasResult) {
					IJavaValue retVal = (IJavaValue) var.getValue();
					if(retVal instanceof IJavaObject) {
						IJavaObject retObj = (IJavaObject) retVal;
						IJavaType javaType = retObj.getJavaType();
						IJavaFieldVariable field = retObj.getField("detailMessage", true);
						String msg = field == null ? "" : field.getValue().getValueString(); 
						return new Exc(javaType.getName(), msg);
					}
				}
			}
		}
		return null;
	}

	// must be invoked under executeInternal(..)
	private void handleFrames(IJavaThread thread) throws DebugException {
		assert thread != null;
		if(thread.isSystemThread() || thread.isDaemon())
			return;

		if(introScreen != null) {
			introScreen.dispose();
			introScreen = null;
		}

		if(thread.getLaunch() != launch) {
			launch = thread.getLaunch();
			runtime = new RuntimeModel();
			if(runtimeView != null)
				runtimeView.dispose();
			runtimeView = new RuntimeViewer(parent);
			runtimeView.setInput(runtime);
			runtimeView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			runtimeView.requestLayout();
		}

		contextService.activateContext(PandionJConstants.CONTEXT_ID);

		runtime.update(thread);

		if(!runtime.isEmpty() && !runtime.isTerminated()) {
			StackFrameModel frame = runtime.getTopFrame();
			IFile sourceFile = frame.getSourceFile();
			int lineNumber = frame.getLineNumber();
			if(sourceFile != null && lineNumber != -1)
				;//PandionJUI.navigateToLine(sourceFile, lineNumber);
		}
	}


	public void executeInternal(PandionJUI.DebugRun r) {
		try {
			r.run();
		}
		catch(DebugException e) {
			e.printStackTrace();
			runtime.setTerminated();
		}
	}

	public <T> T executeInternal(PandionJUI.DebugOperation<T> r, T defaultValue) {
		try {
			return r.run();
		}
		catch(DebugException e) {
			e.printStackTrace();
			runtime.setTerminated();
			return null;
		}
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


	public void terminateProcess() {
		try {
			if(launch != null)
				launch.terminate();
		} catch (DebugException e) {
			if(runtime != null)
				runtime.setTerminated();
		}
		logListener.clear();
	}



	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			System.out.println("CONSOLE: " + existing[i].getName());
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}

	public static ErrorHandler getErrorHandler() {
		return instance.logListener;
	}

}
