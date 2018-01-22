package pt.iscte.pandionj;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import pt.iscte.pandionj.extensibility.FontStyle;
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

		//		populateToolBar();

		addErrorReporting();
	}

	

	private String enc(String p) {
		if (p == null)
			p = "";
		try {
			return URLEncoder.encode(p, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException();
		}
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
		pluginLabel.setText("<a>view installed tags (@)</a>");
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
					info += desc.tag + "\n" + where + "\n";
				}
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Installed tags", info);
			}
		});

		
		Label labelInit = new Label(introComp, SWT.WRAP);
		FontManager.setFont(labelInit, PandionJConstants.MESSAGE_FONT_SIZE, FontStyle.ITALIC);
		labelInit.setForeground(ColorConstants.gray);
		labelInit.setText(PandionJConstants.Messages.START);
		labelInit.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

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
							if(f != null && f.getLineNumber() == -1)
								thread.resume(); // to jump over injected code
						}
						else {
							thread.stepReturn();
						}

						//						Job job = Job.create("Update table", (ICoreRunnable) monitor -> {
						//							System.out.println("STEP");
						//							thread.stepInto();
						//						});
						//						job.schedule(3000);

					}
					else if(e.getKind() == DebugEvent.CHANGE && e.getDetail() == DebugEvent.CONTENT) {
						runtime = new RuntimeModel();
						runtimeView.setInput(runtime);
					}
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
				int line = exceptionFrame.getLineNumber();
				frame.processException(exception, line);
			}
		}); 
	}

	// must be invoked under executeInternal(..)
	private void handleFrames(IJavaThread thread) throws DebugException {
		assert thread != null;

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




	private void populateToolBar() {
		toolBar = getViewSite().getActionBars().getToolBarManager();
		addToolbarAction("Run garbage collector", false, PandionJConstants.TRASH_ICON, PandionJConstants.Messages.TRASH, () -> runtime.simulateGC());

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


	public void terminateProcess() {
		try {
			if(launch != null)
				launch.terminate();
		} catch (DebugException e) {
			if(runtime != null)
				runtime.setTerminated();
		}
	}



	private void addErrorReporting() {
		//		if(System.getProperty("PandionJDebug") != null)
		Platform.addLogListener(new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				Throwable throwable = status.getException();
				Throwable cause = throwable.getCause();
				if(cause != null)
					throwable = cause;
				StackTraceElement[] stackTrace = throwable.getStackTrace();
				for(StackTraceElement e : stackTrace)
					if(e.getClassName().startsWith(PandionJConstants.PLUGIN_ID)) {
						MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "PandionJ Error", null,
								"An error has ocurred. Would you like to send us an error report, helping to improve PandionJ?", 
								MessageDialog.ERROR, new String[] { "Send Error Report", "Ignore" }, 0);
						int result = dialog.open();

						if(result == 1)
							return;

						StringBuffer buf = new StringBuffer();
						buf.append("PandionJ Error Report\n\n");
						buf.append( throwable.getClass().getName() + " : " + throwable.getMessage() + "\n\n");
						buf.append("Exception trace: \n\n");

						for (StackTraceElement el : throwable.getStackTrace()) {
							buf.append(el.toString() + "\n");
						}

						buf.append("\n\nUser code: \n\n");

						status.getException().printStackTrace();
						IWorkbench wb = PlatformUI.getWorkbench();
						IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
						IWorkbenchPage page = window.getActivePage();

						IEditorPart editor = page.getActiveEditor();
						IEditorInput input = editor.getEditorInput();
						int line = -1;
						if (editor instanceof ITextEditor) {
							ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
							ISelection selection = selectionProvider.getSelection();
							if (selection instanceof ITextSelection) {
								ITextSelection textSelection = (ITextSelection) selection;
								line = textSelection.getStartLine() + 1;
							}
						}
						IPath path = ((FileEditorInput)input).getPath();
						IFile file =  ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

						try {
							Scanner scanner = new Scanner(file.getContents());
							int i = 0;
							while(scanner.hasNextLine()) {
								String nextLine = scanner.nextLine();
								i++;
								if(i == line)
									buf.append(">>>>" + nextLine + "\n");
								else
									buf.append(nextLine + "\n");
							}
							scanner.close();
						} catch (CoreException e1) {
							e1.printStackTrace();
						}

						if(runtime != null) {
							buf.append("\n\nCall stack:\n\n");
							for (StackFrameModel frame : runtime.getFilteredStackPath())
								buf.append(frame + "\n");
						}
						buf.append("\n\n");

						IProject project = file.getProject();
						String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
						IFile errorFile = project.getFile("ERROR " + timeStamp + ".txt");
						//							IFile errorImageFile = project.getFile()
						try {
							errorFile.create(new ByteArrayInputStream(buf.toString().getBytes()), true, new NullProgressMonitor());
						} catch (CoreException e1) {
							e1.printStackTrace();
						}
						Program.launch("mailto:andre.santos@iscte-iul.pt?subject=PandionJ%20Error&body=" + enc(buf.toString()) + "&attachment=/Users/andresantos/git/pandionj2/pt.iscte.pandionj/src/pt/iscte/pandionj/Utils.java");
						return;
					}
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
