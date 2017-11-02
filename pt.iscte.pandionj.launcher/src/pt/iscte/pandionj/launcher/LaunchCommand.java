package pt.iscte.pandionj.launcher;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.extensibility.PandionJUI.InvocationAction;

public class LaunchCommand extends AbstractHandler {
	public static final String RUN_LAST_PARAM_ID = "pt.iscte.pandionj.launcher.runParameter";

	private String args;

//	private Shell shell;
//	private InvocationArea area;
//
//	public LaunchCommand() {
//		shell = new Shell(Display.getDefault(), SWT.APPLICATION_MODAL);
//		shell.setLayout(new FillLayout());
//		area = new InvocationArea(shell);
//		shell.addListener(SWT.Traverse, new Listener() {
//			public void handleEvent(Event event) {
//				switch (event.detail) {
//				case SWT.TRAVERSE_ESCAPE:
//					shell.setVisible(false);
//					event.detail = SWT.TRAVERSE_NONE;
//					event.doit = false;
//					break;
//				}
//			}
//		});
//	}
	
	
	
//	@Override
//	public boolean isEnabled() {
		//		IWorkbench wb = PlatformUI.getWorkbench();
		//		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		//		IWorkbenchPage page = window.getActivePage();
		//		IEditorPart editor = page.getActiveEditor();
		//		if(editor == null)
		//			return false;
		//
		//		IEditorInput input = editor.getEditorInput();
		//		return !Activator.isExecutingLaunch() && input instanceof FileEditorInput && input.getName().endsWith(".java");
//		return true;
//	}

	@Override
	public void setEnabled(Object evaluationContext) {
		setBaseEnabled(isEnabled());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String param = event.getParameter(RUN_LAST_PARAM_ID);
		boolean last = param != null && param.equals("true");

		if(Activator.isExecutingLaunch()) {
			boolean terminate = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Terminate", "Another program is executing, would you like to terminate it?");
			if(terminate)
				PandionJUI.terminateProcess();
			else
				return null;
			//			try {
			//				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			//				handlerService.executeCommand("org.eclipse.debug.ui.commands.RunToLine", null);
			//			} 
			//			catch (NotDefinedException | NotEnabledException | NotHandledException e) {
			//
			//			}
		}
		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			IEditorPart editor = page.getActiveEditor();
			IEditorInput input = editor.getEditorInput();
			IPath path = ((FileEditorInput)input).getPath();
			IFile file =  ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			IJavaProject javaProj = JavaCore.create(file.getProject());
			IPath p = file.getProjectRelativePath();

			int line = -1;
			int offset = -1;
			if (editor instanceof ITextEditor) {
				ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
				ISelection selection = selectionProvider.getSelection();
				if (selection instanceof ITextSelection) {
					ITextSelection textSelection = (ITextSelection) selection;
					line = textSelection.getStartLine() + 1;
					offset = textSelection.getOffset();
				}
			}

			if(PandionJUI.hasCompilationErrors(file)) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						"Compilation errors",
						"This file has compilation errors, please fix them before launching.");
				return null;
			}


			final int lineFinal = line;
			IJavaElement e = javaProj.findElement(p.removeFirstSegments(1));
			if(e == null)
				e = javaProj.findElement(new Path(p.lastSegment()));

			if(e instanceof ICompilationUnit) {
				ICompilationUnit unit = (ICompilationUnit) e;
				IType[] types = unit.getTypes();
				IType type = null;
				for(IType t : types)
					if(withinSourceRange(t, offset))
						type = t;

				if(type == null) {
					showMethodMessage();
					return null;
				}


				final String agentArgs = type.getFullyQualifiedName().replace('.', '/');

				IMethod mainMethod = type.getMethod("main", new String[] {"[QString;"}); // TODO other cases

				// normal main()
				if(mainMethod.exists() && mainMethod.isMainMethod() && PandionJUI.checkView()) {
					launch(file, line, type, agentArgs, mainMethod);
				}
				else {
					boolean launchInit = false;
					for (IInitializer init : type.getInitializers()) {
						if(withinSourceRange(init, offset)) {
							launch(file, line, type, "", mainMethod); // FIXME estoira
							launchInit = true;
							break;
						}
					}

					if(!launchInit) {
						// run last
						if(last && args != null) {
							if(PandionJUI.checkView()) {
								String typeName = args.substring(0, args.indexOf('|'));
								IType t = ((ICompilationUnit) e).getType(typeName);
								launch(file, lineFinal, t, args, mainMethod);
							}
						}
						else {
							IMethod selectedMethod = null;
							for (IMethod m : type.getMethods()) {
								if(Modifier.isStatic(m.getFlags()) && withinSourceRange(m, offset)) {							
									selectedMethod = m;
									break;
								}
							}
							if(selectedMethod == null) {
								showMethodMessage();
								return null;
							}
							String methodSig = getResolvedSignature(type, selectedMethod);
							if(selectedMethod.getParameterTypes().length != 0) {
								final IType t = type;

								InvocationAction action = new InvocationAction() {
									@Override
									public void invoke(String expression, String[] paramValues) {
//										shell.setVisible(false);
										args = agentArgs + "|" + expression.replaceAll("\"", "\\\\\"") + "|" + methodSig;
										try {
											launch(file, lineFinal, t, args, mainMethod);
										} catch (CoreException e) {
											e.printStackTrace();
										}
									}
								};
								PandionJUI.openInvocation(selectedMethod, action);
								
//								area.setMethod(file, selectedMethod, new InvocationAction() {
//									@Override
//									public void invoke(String expression) {
//										shell.setVisible(false);
//										args = agentArgs + "|" + expression.replaceAll("\"", "\\\\\"") + "|" + methodSig;
//										try {
//											launch(file, lineFinal, t, args, mainMethod);
//										} catch (CoreException e) {
//											e.printStackTrace();
//										}
//									}
//								});

//								shell.pack();
//								Rectangle screen = Display.getCurrent().getClientArea();
//								Point cursor = Display.getCurrent().getCursorLocation();
//								int w = screen.width-cursor.x-shell.getSize().x-20;
//								if(w < 0)
//									cursor = new Point(cursor.x + w, cursor.y);
//								shell.setLocation(cursor);
//								if(shell.getVisible())
//									shell.open();
//								else
//									shell.setVisible(true);
//
//								shell.setFocus();

								//									while (!shell.isDisposed()) {
								//										if (!display.readAndDispatch())
								//											display.sleep();
								//									}

								//									PandionJUI.promptInvocation(file, selectedMethod, new InvocationAction() {
								//										@Override
								//										public void invoke(String expression) {
								//											args = agentArgs + "|" + expression.replaceAll("\"", "\\\\\"") + "|" + methodSig;
								//											try {
								//												launch(file, lineFinal, t, args, mainMethod);
								//											} catch (CoreException e) {
								//												e.printStackTrace();
								//											}
								//											
								//										}
								//									});
							}
							else {  // no params
								if(PandionJUI.checkView()) {
									args = agentArgs + "|" + selectedMethod.getElementName() + "()" + "|" + methodSig;
									launch(file, line, type, args, mainMethod);
								}
							}
						}
					}
				}
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getResolvedSignature(IType type, IMethod method) throws JavaModelException {
		String sig = "(";
		for (String p : method.getParameterTypes()) {
			sig += resolve(p, type);
		}
		sig += ")" + resolve(method.getReturnType(), type);
		return sig;
		
	}

	private static String resolve(String ret, IType type) throws JavaModelException {
		if(!ret.matches("\\[*Q(.)+"))
			return ret;
		
		int i = ret.indexOf('Q');
		String q = ret.substring(i);
		String[][] resolve = type.resolveType(Signature.getSignatureSimpleName(q));
		String t = "";
		if(resolve != null && resolve.length > 0) {
			t += "L"; 
			if(!resolve[0][0].isEmpty()) {
				t += resolve[0][0].replace('.', '/');
				t += "/";
			}
			t += resolve[0][1] + ";";
		}
		return t.isEmpty() ? ret : ret.substring(0, i) + t;
	}
	private static boolean withinSourceRange(IMember member, int offset) throws JavaModelException {
		ISourceRange sourceRange = member.getSourceRange();
		return offset >= sourceRange.getOffset() && offset <= sourceRange.getOffset()+sourceRange.getLength();
	}

	private static void showMethodMessage() {
		MessageDialog.openError(Display.getDefault().getActiveShell(),
				"Please select method",
				"Place the cursor at a line of the body of a static method.");
	}

	private void launch(IResource file, int line, IType type, String agentArgs, IMethod mainMethod)
			throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType confType = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy wc = confType.newInstance(null, file.getName() + " (PandionJ)");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, file.getProject().getName());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);

		//		if(breakPoint != null)
		//			breakPoint.delete();
		//		if(line != -1)
		//			breakPoint = JDIDebugModel.createLineBreakpoint(file, firstType.getFullyQualifiedName(), line, -1, -1, 0, true, null);

		try {	
			Bundle bundle = Platform.getBundle(LaunchCommand.class.getPackage().getName());
			URL find = FileLocator.find(bundle, new Path("lib/agent.jar"), null);
			URL resolve = FileLocator.resolve(find);
			if(!mainMethod.exists()) {
				String path = resolve.getPath();
				if(Platform.getOS().compareTo(Platform.OS_WIN32) == 0)
					path = path.substring(1);

				String args =  "-javaagent:\"" + path + "=" + agentArgs + "\"";
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, args);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ILaunchConfiguration config = wc.doSave();
		Activator.launch(config);
	}


	
}

