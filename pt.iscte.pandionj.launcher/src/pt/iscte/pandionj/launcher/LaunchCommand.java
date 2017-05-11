package pt.iscte.pandionj.launcher;

import java.io.IOException;
import java.net.URL;

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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

public class LaunchCommand extends AbstractHandler {
	private ILaunch launch;
	private IJavaLineBreakpoint breakPoint;

	@Override
	public boolean isEnabled() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		IEditorInput input = editor.getEditorInput();
		return input instanceof FileEditorInput && input.getName().endsWith(".java");
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if(launch != null && !launch.isTerminated())
				launch.terminate();

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
			if (editor instanceof ITextEditor) {
			  ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
			  ISelection selection = selectionProvider.getSelection();
			  if (selection instanceof ITextSelection) {
			    ITextSelection textSelection = (ITextSelection) selection;
			    line = textSelection.getStartLine() + 1;
			  }
			}
			
			IJavaElement e = javaProj.findElement(p.removeFirstSegments(1));
			if(e == null)
				e = javaProj.findElement(new Path(p.lastSegment()));
			// TODO package in name
			if(e != null) {
				IType firstType = ((ICompilationUnit) e).getTypes()[0];
				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
				ILaunchConfigurationWorkingCopy wc = type.newInstance(null, file.getName() + " (PandionJ)");
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, file.getProject().getName());
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, firstType.getFullyQualifiedName());
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
				
				if(breakPoint != null)
					breakPoint.delete();
				
				if(line != -1)
					breakPoint = JDIDebugModel.createLineBreakpoint(file, firstType.getFullyQualifiedName(), line, -1, -1, 0, true, null);
				
				try {
					Bundle bundle = Platform.getBundle(LaunchCommand.class.getPackage().getName());
					URL find = FileLocator.find(bundle, new Path("lib/agent.jar"), null);
					URL resolve = FileLocator.resolve(find);
					wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-javaagent:" + resolve.getPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				ILaunchConfiguration config = wc.doSave();
				launch = config.launch(ILaunchManager.DEBUG_MODE, null, true);
				
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}

