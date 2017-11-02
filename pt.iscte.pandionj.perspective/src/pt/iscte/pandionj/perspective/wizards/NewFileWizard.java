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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

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
	 * This method is called when 'Finish' button is pressed in the wizard. We will
	 * create an operation and run it using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		Object e = selection.getFirstElement();

		if (selection.size() != 1 || !(e instanceof IJavaElement)) {
			MessageDialog.openError(null, "Select container", "Please select a single project or package where to create the file.");
			return false;
		}
		IJavaElement element = (IJavaElement) e;
		IJavaProject proj = element.getJavaProject();
		IPackageFragmentRoot root = null;
		if(!(e instanceof IPackageFragment)) {
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
		}

		final IPath containerPath = root != null ? root.getPath() : element.getPath();
		final String fileName = page.getFileName();
		final String packageName = e instanceof IPackageFragment && !((IPackageFragment) e).isDefaultPackage() ? ((IPackageFragment) e).getElementName() : null;
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerPath, fileName, packageName, monitor);
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

	private void doFinish(IPath containerPath, String fileName, String packageName, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(containerPath);
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerPath + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(fileName.substring(0, fileName.indexOf('.')), packageName);
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
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				try {
					IDE.openEditor(page, file);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	
	}

	private static final String NL = System.getProperty("line.separator"); 
	private InputStream openContentStream(String className, String packageName) {
		String contents = "";
		if(packageName != null)
			contents += "package " + packageName + ";" + NL + NL;
			
		contents += "class " + className + " {" + NL + NL + "}";
		return new ByteArrayInputStream(contents.getBytes());
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