package pt.iscte.pandionj.extensibility;


import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.PandionJView;

public interface PandionJUI {

	interface InvocationAction {
		void invoke(String expression);
	}

	static void promptInvocation(IMethod m, InvocationAction a) {
		if(m.getNumberOfParameters() != 0)
			PandionJView.getInstance().promptInvocation(m, a);
	}

	static void navigateToLine(IFile file, Integer line) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(IMarker.LINE_NUMBER, line);
		IMarker marker = null;
		try {
			marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);
			IMarker finalMarker = marker;
			executeUpdate(() -> {
				try {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ITextEditor editor = (ITextEditor) IDE.openEditor(activePage, file);
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				int offset = document.getLineOffset(line);
				editor.selectAndReveal(offset, 0);
//					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), finalMarker);
				} catch ( PartInitException e ) {
					//complain
				} catch (BadLocationException e) {

				}
			});
		} catch ( CoreException e1 ) {
			//complain
		} finally {
			try {
				if (marker != null)
					marker.delete();
			} catch ( CoreException e ) {
				//whatever
			}
		}
	}
	
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
