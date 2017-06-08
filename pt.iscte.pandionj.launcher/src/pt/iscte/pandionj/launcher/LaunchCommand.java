package pt.iscte.pandionj.launcher;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
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

public class LaunchCommand extends AbstractHandler {
	private IJavaLineBreakpoint breakPoint;

	@Override
	public boolean isEnabled() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		IEditorInput input = editor.getEditorInput();
		return input instanceof FileEditorInput && input.getName().endsWith(".java") && !Activator.isExecutingLaunch();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			IEditorPart editor = page.getActiveEditor();
			IEditorInput input = editor.getEditorInput();
			IPath path = ((FileEditorInput)input).getPath();
			IResource file =  ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
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

			IJavaElement e = javaProj.findElement(p.removeFirstSegments(1));
			if(e == null)
				e = javaProj.findElement(new Path(p.lastSegment()));
			// TODO package in name
			if(e != null) {
				IType firstType = ((ICompilationUnit) e).getTypes()[0];
				String agentArgs = firstType.getFullyQualifiedName().replace('.', '/');
				
				IMethod mainMethod = firstType.getMethod("main", new String[] {"[QString;"});
//				if(!mainMethod.exists())
//					mainMethod = firstType.getMethod("main", new String[0]);

				if(!mainMethod.exists()) {
					IMethod selectedMethod = null;
					for (IMethod m : firstType.getMethods()) {
						ISourceRange sourceRange = m.getSourceRange();
						if(Modifier.isStatic(m.getFlags()) && offset >= sourceRange.getOffset() && offset <= sourceRange.getOffset()+sourceRange.getLength()) {							
							selectedMethod = m;
							break;
						}
					}
					if(selectedMethod == null) {
						MessageDialog.openError(Display.getDefault().getActiveShell(),"No selected method", "???");
						return null;
					}
					else {
						agentArgs += ";" + PandionJUI.promptInvocationExpression(selectedMethod);
					}
				}


				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
				ILaunchConfigurationWorkingCopy wc = type.newInstance(null, file.getName() + " (PandionJ)");
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, file.getProject().getName());
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, firstType.getFullyQualifiedName());
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);

				if(breakPoint != null)
					breakPoint.delete();

				if(line != -1) {
					Map<String, Object> attributes = new HashMap<String, Object>(4);
					attributes.put(IBreakpoint.PERSISTED, Boolean.FALSE);
					attributes.put("org.eclipse.jdt.debug.ui.run_to_line", Boolean.TRUE);
					breakPoint = JDIDebugModel.createLineBreakpoint(file, firstType.getFullyQualifiedName(), line, -1, -1, 0, true, null);
//					breakPoint = JDIDebugModel.createLineBreakpoint(file, firstType.getFullyQualifiedName(), line, -1, -1, 0, true, null);
				}
				try {
					Bundle bundle = Platform.getBundle(LaunchCommand.class.getPackage().getName());
					URL find = FileLocator.find(bundle, new Path("lib/agent.jar"), null);
					URL resolve = FileLocator.resolve(find);
					if(!mainMethod.exists()) {
						String args =  "-javaagent:" + resolve.getPath() + "=" + agentArgs;
						wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, args);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				ILaunchConfiguration config = wc.doSave();
				Activator.launch(config);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}


}

