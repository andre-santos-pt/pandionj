package pt.iscte.pandionj.perspective.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class NewPackageWizard extends Wizard implements INewWizard {
	private NewFolderWizardPage page;
	private IStructuredSelection selection;

	public NewPackageWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new NewFolderWizardPage(selection);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		Object e = selection.getFirstElement();
		if (e instanceof IJavaElement)
			e = ((IJavaElement) e).getJavaProject();

		if (selection.size() != 1 || !(e instanceof IJavaProject)) {
			MessageDialog.openError(null, "Select project", "Please select a project (root).");
			return false;
		}

		IJavaProject proj = (IJavaProject) e;
		IPackageFragmentRoot root = null;
		try {
			IPackageFragmentRoot[] roots = proj.getAllPackageFragmentRoots();
			for (IPackageFragmentRoot r : roots)
				if (r.getKind() == IPackageFragmentRoot.K_SOURCE) {
					root = r;
					break;
				}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}

		final IPath containerPath = root != null ? root.getPath() : proj.getPath();
		final String[] path = page.getPath();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerPath, path, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException ex) {
			return false;
		} catch (InvocationTargetException ex) {
			Throwable realException = ex.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	private void doFinish(IPath containerPath, String[] path, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Creating package " + String.join(".", path), path.length);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IResource resource = root.findMember(containerPath);
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerPath + "\" does not exist.");
		}

		IContainer container = (IContainer) resource;
		for(int i = 0; i < path.length; i++) {
			Path p = new Path(path[i]);
			final IFolder folder = container.getFolder(p);
			if(!folder.exists())
				folder.create(true, false, monitor);
			container = container.getFolder(p);
			monitor.worked(1);
		}
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "pt.iscte.perspective", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

}