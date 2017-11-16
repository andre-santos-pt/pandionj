package pt.iscte.pandionj.extensibility;


import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.Figure;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

import pt.iscte.pandionj.ColorManager;
import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.InvokeDialog;
import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.ParserManager;
import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.parser.VarParser;

public interface PandionJUI {

	interface InvocationAction {
		void invoke(String expression, String[] paramValues);
	}

//	static void promptInvocation(IFile file, IMethod m, InvocationAction a) {
//		PandionJView view = PandionJView.getInstance();
//		if(view == null)
//			view = openViewDialog();
//
//		if(view != null && m.getNumberOfParameters() != 0)
//			view.promptInvocation(file, m, a);
//	}

	static PandionJView openViewDialog() {
		if(MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Open PandionJ view", Constants.Messages.RUN_DIALOG)) {
			try {
				return (PandionJView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(Constants.VIEW_ID);
			} catch (PartInitException e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Open PandionJ view", "View could not be opened.");
			}
		}
		return null;
	}

	static boolean checkView() {
		PandionJView view = PandionJView.getInstance();
		if(view == null)
			view = openViewDialog();
		
		return view != null;
	}
	
	static void activateEditor() {
		PandionJView instance = PandionJView.getInstance();
		if(instance != null)
			instance.setFocus();
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(instance);
//		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		IViewReference viewRef = activePage.findViewReference(Constants.VIEW_ID);
//		viewRef.getPart(false).getSite().getSelectionProvider().setSelection(StructuredSelection.EMPTY);

	}
	
	static boolean hasCompilationErrors(IFile file) {
		VarParser parser = ParserManager.getVarParserResult(file);
		return parser.hasErrors();
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
		Display.getDefault().asyncExec(() -> {
//			System.out.println("UPDATE " + r.hashCode());
			PandionJView.getInstance().executeInternal(r);
			RuntimeViewer.getInstance().updateLayout();
//			System.out.println("/UPDATE " + r.hashCode());
		});
	}

	static Color getColor(int r, int g, int b) {
		return ColorManager.getColor(truncate(r), truncate(g), truncate(b));
	}
	
	static int truncate(int r) {
		if(r < 0) return 0;
		if(r > 255) return 255;
		else return r;
	}

	static void setFont(Figure fig, int size) {
		FontManager.setFont(fig, size);
	}

	static void terminateProcess() {
		PandionJView.getInstance().terminateProcess();
	}

	static void openInvocation(IMethod method, InvocationAction action) {
		InvokeDialog dialog = new InvokeDialog(Display.getDefault().getActiveShell(), method, action);
//		dialog.setMethod(method, action);
		dialog.open();
//		if(dialog.open() == Window.OK)
//			System.out.println("???");
		
//		InvocationWidget.open(method, action);
	}
	
}
