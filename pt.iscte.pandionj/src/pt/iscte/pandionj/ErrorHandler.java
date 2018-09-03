package pt.iscte.pandionj;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.model.StackFrameModel;

class ErrorHandler implements ILogListener {

	private PandionJView view;
	private Set<Integer> errors;

	public ErrorHandler(PandionJView view) {
		this.view = view;
		errors = new HashSet<Integer>();
	}
	
	public void clear() {
		errors.clear();
	}

	@Override
	public void logging(IStatus status, String plugin) {
		Throwable throwable = status.getException();
		if(throwable == null)
			return;
		Throwable cause = throwable.getCause();
		if(cause != null)
			throwable = cause;
		StackTraceElement[] stackTrace = throwable.getStackTrace();

		RuntimeModel runtime = view.getRuntime();
		
		if(!hasPandionFrame(stackTrace) || runtime == null)
			return;
		
		String errorKey = stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ":" + stackTrace[0].getLineNumber();
		int errorHash = errorKey.hashCode();
		//		int errorHash = calcErrorHash(throwable);
		if(errors.contains(errorHash))
			return;

		errors.add(errorHash);
		
		StackFrameModel topFrame = runtime.getTopFrame();
		if(topFrame == null)
			return;

		IFile srcFile = topFrame.getSourceFile();
		if(srcFile == null)	
			return;

		MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "PandionJ Error", null,
				"An error has ocurred. Would you like to send us an error report for helping to improve PandionJ?", 
				MessageDialog.ERROR, new String[] { "Send Error Report", "Ignore" }, 0);
		int result = dialog.open();

		if(result == 1)
			return;


		StringBuffer buf = new StringBuffer();
		buf.append("PandionJ Error Report\n\n");
		appendStackTrace(throwable, errorHash, buf);

		int line = getFrameLine(topFrame);

		buf.append("\n\nUser code: \n\n");
		appendUserCode(srcFile, buf, line);

		appendUserCallStack(runtime, buf);

		IProject project = srcFile.getProject();

		String fileName = getFileName() + " " + errorHash;

		IFile imgErrorFile = project.getFile(fileName + ".png");
		Image image = RuntimeViewer.getInstance().getCanvasImage();
		Utils.saveImageToPNGandDispose(image, imgErrorFile);
		
		IFile errorFile = project.getFile(fileName + ".txt");
		try {
			errorFile.create(new ByteArrayInputStream(buf.toString().getBytes()), true, new NullProgressMonitor());
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		
		Utils.sendEmail("Error Report", buf);
	}

	public void reportBug() {
		RuntimeModel runtime = view.getRuntime();
		
		if(runtime == null)
			return;
		
		StackFrameModel topFrame = runtime.getTopFrame();
		if(topFrame == null)
			return;
		
		IFile srcFile = topFrame.getSourceFile();
		if(srcFile == null)
			return;
		
		BugReportDialog dialog = new BugReportDialog(Display.getDefault().getActiveShell(), "Bug Report", "Please describe what is wrong. You do not need to write your code, it will be copied automatically.", 
				"", null);
		
		if(dialog.open() != InputDialog.OK)
			return;
		
		
		StringBuffer buf = new StringBuffer();
		
		buf.append(dialog.getValue() + "\n\n----------------------------\n\n");

		appendUserCode(srcFile, buf, getFrameLine(topFrame));
		
		IProject project = srcFile.getProject();
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
		String fileName = "BUG " + timeStamp;

		IFile imgErrorFile = project.getFile(fileName + ".png");
		Image image = RuntimeViewer.getInstance().getCanvasImage();
		Utils.saveImageToPNGandDispose(image, imgErrorFile);

		IFile errorFile = project.getFile(fileName + ".txt");
		try {
			errorFile.create(new ByteArrayInputStream(buf.toString().getBytes()), true, new NullProgressMonitor());
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	
		MessageDialog senddialog = new MessageDialog(Display.getDefault().getActiveShell(), "Bug Report", null,
				"Two files were created in your project. One containing your message and source code, and another with the screenshot of the current PandionJ view.",
				MessageDialog.QUESTION, new String[] { "Send Email", "Close" }, 0);
		int result = senddialog.open();

		if(result == 0)
			Utils.sendEmail("Bug Report", buf);
	}
	
	private int getFrameLine(StackFrameModel topFrame) {
		int line = -1;
		try {
			line = topFrame.getStackFrame().getLineNumber();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return line;
	}
	
	private int calcErrorHash(Throwable throwable) {
		int hash = 0;
		for (StackTraceElement el : throwable.getStackTrace())
			hash += (el.getClassName() + el.getLineNumber()).hashCode();

		return hash;
	}

	private boolean hasPandionFrame(StackTraceElement[] stackTrace) {
		for(StackTraceElement e : stackTrace)
			if(e.getClassName().startsWith(PandionJConstants.PLUGIN_ID))
				return true;

		return false;
	}
	
	private String getFileName() {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
		String fileName = "ERROR " + timeStamp;
		return fileName;
	}

	static void appendUserCode(IFile srcFile, StringBuffer buf, int lineMark) {
		try {
			Scanner scanner = new Scanner(srcFile.getContents(true));
			int i = 0;
			while(scanner.hasNextLine()) {
				String nextLine = scanner.nextLine();
				i++;
				if(i == lineMark)
					buf.append(">>>>" + nextLine + "\n");
				else
					buf.append(nextLine + "\n");
			}
			scanner.close();
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}
	
	
	private void appendUserCallStack(RuntimeModel runtime, StringBuffer buf) {
		buf.append("\n\nCall stack:\n\n");
		for (StackFrameModel frame : runtime.getFilteredStackPath())
			buf.append(frame + "\n");
		buf.append("\n");
	}

	private void appendStackTrace(Throwable throwable, int errorHash, StringBuffer buf) {
		buf.append(throwable.getClass().getName() + " : " + throwable.getMessage() + "\n\n");

		buf.append("Exception trace: \n\n");
		for (StackTraceElement el : throwable.getStackTrace())
			buf.append(el.toString() + "\n");

		buf.append("Error code: " + errorHash);
	}
	
	private static class BugReportDialog extends InputDialog {
		public BugReportDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
		  Control result = super.createDialogArea(parent);

		  Text text = getText();  // The input text

		  GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		  data.heightHint = convertHeightInCharsToPixels(5); // number of rows 
		  text.setLayoutData(data);

		  return result;
		}

		@Override
		protected int getInputTextStyle() {
		  return SWT.MULTI | SWT.BORDER;
		}
	}
}