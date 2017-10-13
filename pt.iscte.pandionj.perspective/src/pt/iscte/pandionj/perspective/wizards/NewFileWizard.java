package pt.iscte.pandionj.perspective.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;


public class NewFileWizard extends Wizard implements INewWizard {
	private NewFileWizardPage page;
	private IStructuredSelection selection;

	/**
	 * Constructor for NewFileWizard.
	 */
	public NewFileWizard() {
		super();
		setNeedsProgressMonitor(true);
	}



	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		page = new NewFileWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		Object e = selection.getFirstElement();
		if(e instanceof IJavaElement)
			e = ((IJavaElement) e).getJavaProject();

		if(selection.size() != 1 || !(e instanceof IJavaProject)) {
			MessageDialog.openError(null, "Select project", "Please select a project (root).");
			return false;
		}

		IJavaProject proj = (IJavaProject) e;
		IPackageFragmentRoot root = null;
		try {
			IPackageFragmentRoot[] roots = proj.getAllPackageFragmentRoots();
			for(IPackageFragmentRoot r : roots)
				if(r.getKind() == IPackageFragmentRoot.K_SOURCE) {
					root = r;
					break;
				}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}

		final IPath containerPath = root != null ? root.getPath() : proj.getPath();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerPath, fileName, monitor);
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


	private void doFinish(
			IPath containerPath,
			String fileName,
			IProgressMonitor monitor)
					throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(containerPath);
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerPath + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(fileName.substring(0, fileName.indexOf('.')));
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				try {
					IDE.openEditor(page, file);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	private InputStream openContentStream(String className) {
		String contents = "class " + className + " {" + System.getProperty("line.separator") + System.getProperty("line.separator") + "}";
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
				new Status(IStatus.ERROR, "pt.iscte.perspective", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}


}