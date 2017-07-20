package pt.iscte.pandionj.extensibility;


import java.net.URL;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;
import pt.iscte.pandionj.model.ReferenceModel;
import pt.iscte.pandionj.model.StackFrameModel;

public interface PandionJUI {

	interface InvocationAction {
		void invoke(String expression);
	}

	static void promptInvocation(IMethod m, InvocationAction a) {
		if(m.getNumberOfParameters() != 0)
			PandionJView.getInstance().promptInvocation(m, a);
	}

	/**
	 * Open editor and select a given line
	 */
	static void navigateToLine(IFile file, int line) {
		executeUpdate(() -> {
			try {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ITextEditor editor = (ITextEditor) IDE.openEditor(activePage, file);
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				int offset = document.getLineOffset(line);
				editor.selectAndReveal(offset, 0);
			}
			catch ( PartInitException e ) {
				e.printStackTrace();
			} 
			catch (BadLocationException e) {

			}
		});
	}

	/**
	 * Get image contained in the 'images' folder of the plugin
	 */
	static Image getImage(String name) {
		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		URL imagePath = FileLocator.find(bundle, new Path(Constants.IMAGE_FOLDER + "/" + name), null);
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(imagePath);
		return imageDesc.createImage();
	}

	interface DebugOperation<T> {
		T run() throws DebugException;
	}

	interface DebugRun {
		void run() throws DebugException;
	}

	public static <T> T execute(DebugOperation<T> r, T defaultValue)  {
		return PandionJView.getInstance().executeInternal(r, defaultValue);
	}

	public static void execute(DebugRun r) {
		PandionJView.getInstance().executeInternal(r);
	}

	public static void executeUpdate(DebugRun r) {
		Display.getDefault().asyncExec(() -> PandionJView.getInstance().executeInternal(r));
	}
	
	
}
