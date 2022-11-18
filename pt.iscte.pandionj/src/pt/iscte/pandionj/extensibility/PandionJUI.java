package pt.iscte.pandionj.extensibility;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.Figure;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

import pt.iscte.pandionj.ColorManager;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.InvokeDialog;
import pt.iscte.pandionj.PandionJView;
import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.StaticInvocationWidget;
import pt.iscte.pandionj.parser.ParserManager;
import pt.iscte.pandionj.parser.VarParser;

public interface PandionJUI {

	interface InvocationAction {
		void invoke(String expression, String[] paramValues, String[] paramExpressioValues);
	}

	static PandionJView openViewDialog() {
		if(MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Open PandionJ view", PandionJConstants.Messages.RUN_DIALOG)) {
			try {
				return (PandionJView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PandionJConstants.VIEW_ID);
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
		Bundle bundle = Platform.getBundle(PandionJConstants.PLUGIN_ID);
		URL imagePath = FileLocator.find(bundle, new Path(PandionJConstants.IMAGE_FOLDER + "/" + name), null);
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
			PandionJView.getInstance().executeInternal(r);
			RuntimeViewer.getInstance().updateLayout();
		});
	}

	static Color getColor(int r, int g, int b) {
		return ColorManager.getColor(truncate(r), truncate(g), truncate(b));
	}
	
	static Font getFont(int size, FontStyle...styles) {
		return FontManager.getFont(size, styles);
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
		dialog.open();
	}
	
	public static String generateInvocationScript(IMethod method, String invocationExpression, String[] values) {
		invocationExpression = invocationExpression.replace("\"", "\\\\\"");
		StringBuffer buf = new StringBuffer();
		
		int i = 0;
		List<String> args = new ArrayList<>();
		for (String t : method.getParameterTypes()) {
			String pType = Signature.getSignatureSimpleName(t);
			if(StaticInvocationWidget.isArrayType2D(pType)) {
				ASTParser parser = ASTParser.newParser(AST.JLS8);
				parser.setKind(ASTParser.K_EXPRESSION);
				parser.setResolveBindings(true);
				parser.setBindingsRecovery(true);
				parser.setStatementsRecovery(true);
				parser.setSource(values[i].toCharArray());
				
				Expression e = (Expression) parser.createAST(null);
				if(e instanceof NullLiteral)
					buf.append(pType + " a$" + i + " = null;\n");
				else if(e instanceof ArrayInitializer) {
					String baseType = StaticInvocationWidget.baseType(pType);
					ArrayInitializer init = (ArrayInitializer) e;
					buf.append(pType + " a$" + i + " = new " + baseType + "[" + init.expressions().size() + "][]" + ";\n");
					int j = 0;
					for(Object o : init.expressions()) {
						if(o instanceof NullLiteral)
							buf.append("a$" + i + "[" + (j++) + "] = null;\n");
						else
							buf.append("a$" + i + "[" + (j++) + "] = new " + baseType + "[]"  + o.toString() + ";\n");
					}
				}
			}
			else {
				buf.append(pType + " a$" + i + " = " + values[i] + ";\n");
			}
			args.add("a$"+i);
			i++;
		}
		
		String retType = null;
		try {
			retType = Signature.getSignatureSimpleName(method.getReturnType());
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		if(!retType.equals("void"))
			buf.append(retType + " result = ");
		
		buf.append(method.getElementName() + "(" + String.join(", ", args) + ");\n");
		if(!retType.equals("void")) {
			if(StaticInvocationWidget.isArrayType1D(retType)) {
				String baseType = StaticInvocationWidget.baseType(retType);
				buf.append("System.out.print(\""  + invocationExpression + 
						" = {\");\n");
				buf.append("for(int i = 0; i < result.length; i++) {");
				buf.append("if(i != 0) System.out.print(\",\");");
				if(baseType.equals("String"))
					buf.append("System.out.print(\"\\\\\"\" + result[i] + \"\\\\\"\");}");
				else if (baseType.equals("char"))
					buf.append("System.out.print(\"'\" + result[i] + \"'\");}");
				else
					buf.append("System.out.print(result[i]);}");
				buf.append("System.out.println(\"}\");\n");
			}
			else if(StaticInvocationWidget.isArrayType2D(retType)) {
				String baseType = StaticInvocationWidget.baseType(retType);
				String spaces = "";
				for(int s = 0; s < invocationExpression.length() + 4; s++)
					spaces += " ";
				buf.append("System.out.print(\"" + invocationExpression + " = {\");");
				buf.append("for(int i = 0; i < result.length; i++) {");
				
				buf.append("String s = \"{\";\n");
				buf.append("for(int j = 0; j < result[i].length; j++) {\n");
				buf.append("if(j != 0) s += \",\";\n");

				if(baseType.equals("String"))
					buf.append("s += \"\\\\\"\" + result[i][j] + \"\\\\\"\";}");
				else if (baseType.equals("char"))
					buf.append("s += \"'\" + result[i][j] + \"'\";}");
				else
					buf.append("s += result[i][j];}\n");
				buf.append("s += \"}\";\n");
				
				buf.append("if(i != 0) System.out.print(\",\\n" + spaces + "\");");
				buf.append("System.out.print(s);};");
				buf.append("System.out.println(\"}\");");
			}
			else if(retType.equals("String")) {
				buf.append("System.out.print(\""  + invocationExpression + " = \");");
				buf.append("System.out.print(\"\\\\\"\");");
				buf.append("System.out.print(result);");
				buf.append("System.out.println(\"\\\\\"\");");				
			}
			else if(retType.equals("char")) {
				buf.append("System.out.print(\""  + invocationExpression + " = \");");
				buf.append("System.out.print(\"'\");");
				buf.append("System.out.print(result);");
				buf.append("System.out.println(\"'\");");	
			}
			else
				buf.append("System.out.println(\""  + invocationExpression + " = \" + result);");
		}
		
		return buf.toString();
	}
	
}
