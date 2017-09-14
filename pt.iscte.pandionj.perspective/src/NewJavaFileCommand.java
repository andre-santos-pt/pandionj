import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class NewJavaFileCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		System.out.println(selection);
		
		IProject proj = (IProject) ((IStructuredSelection) selection).getFirstElement();
		FileInputDialog dialog = new FileInputDialog(Display.getDefault().getActiveShell());
		String filename = dialog.open();
		final IFile file = proj.getFile(new Path(filename));
		try {
			InputStream stream = openContentStream();
			if (file.exists()) {
				file.setContents(stream, true, true, null);
			} else {
				file.create(stream, true, null);
			}
			stream.close();
		} catch (IOException e) {
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, "pt.iscte.pidesco.javaeditor", true);
				} catch (PartInitException e) {
				}
			}
		});
		return null;
	}

	private InputStream openContentStream() {
		String contents = "class Test {" + System.getProperty("line.separator") + "}";
		return new ByteArrayInputStream(contents.getBytes());
	}
	
	@Override
	public boolean isEnabled() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection s = (IStructuredSelection) selection;
			if(s.size() == 1) {
				Object e = s.getFirstElement();
				try {
					return e instanceof IProject && ((IProject) e).hasNature("org.eclipse.jdt.internal.core.JavaProject");
				} catch (CoreException e1) {
					return false;
				}
			}
		}
		return false;
	}
	


}
