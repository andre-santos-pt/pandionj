package pt.iscte.pandionj.launcher;
import java.util.HashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {
	private static ILaunch launch;
	private Annotation annotation = new Annotation(IDebugUIConstants.ANNOTATION_TYPE_INSTRUCTION_POINTER_CURRENT, false, "");
	private IAnnotationModel annotationModel;


	private IDebugEventSetListener listener =  new IDebugEventSetListener() {
		public void handleDebugEvents(DebugEvent[] events) {
			if(events.length > 0) {
				DebugEvent e = events[0];
				if(e.getKind() == DebugEvent.SUSPEND && (e.getDetail() == DebugEvent.STEP_END || e.getDetail() == DebugEvent.BREAKPOINT)) {
					IThread thread = (IThread) e.getSource();

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								IStackFrame topStackFrame = thread.getTopStackFrame();
								if(topStackFrame == null)
									return;
								int line = topStackFrame.getLineNumber();
								if(line == -1)
									return;
								line--;
								Object obj = thread.getLaunch().getSourceLocator().getSourceElement(thread.getTopStackFrame());
								if(!(obj instanceof IFile))
									return;
								IFile srcFile = (IFile) obj; 
								IWorkbench wb = PlatformUI.getWorkbench();
								IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
								IWorkbenchPage page = window.getActivePage();

								HashMap<String, Object> map = new HashMap<>();
								map.put(IMarker.LINE_NUMBER, new Integer(line+1));
								IMarker marker = srcFile.createMarker(IMarker.TEXT);
								marker.setAttributes(map);
								IEditorPart openEditor = IDE.openEditor(page, marker);
								IEditorInput editorInput = openEditor.getEditorInput();
								marker.delete();

								IDocumentProvider docProvider = ((ITextEditor) openEditor).getDocumentProvider();
								IDocument document = docProvider.getDocument(editorInput);
								if(annotationModel != null)
									annotationModel.removeAnnotation(annotation);
								annotationModel = docProvider.getAnnotationModel(editorInput);
								try {
									int offset = document.getLineOffset(line);
									int length = document.getLineLength(line);
									annotationModel.addAnnotation(annotation, new Position(offset, length));
								} catch (BadLocationException e) {
									e.printStackTrace();
								}
							} catch (DebugException e1) {
								e1.printStackTrace();
							} catch (CoreException e2) {
								e2.printStackTrace();
							}
						}
					});
				}
				else if(e.getKind() == DebugEvent.TERMINATE) {
					if(annotationModel != null)
						annotationModel.removeAnnotation(annotation);
				}
			}
		}


	};

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		DebugPlugin.getDefault().addDebugEventListener(listener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		DebugPlugin.getDefault().removeDebugEventListener(listener);
	}

	static void launch(ILaunchConfiguration config) {
		if(launch != null && !launch.isTerminated())
			try {
				launch.terminate();
			} catch (DebugException e) {
				e.printStackTrace();
			}

		try {
			launch = config.launch(ILaunchManager.DEBUG_MODE, null, true);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	static boolean isExecutingLaunch() {
		return launch != null && !launch.isTerminated() && launch.getDebugTarget() != null;
	}

	static void resume(ExecutionEvent event) {
		if(launch != null) {
			try {
				IDebugTarget debugTarget = launch.getDebugTarget();
				if(debugTarget != null)
					debugTarget.resume();
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		//		showView(event);
	}

	static void stepInto(ExecutionEvent event) {
		if(launch != null) {
			try {
				for(IThread t : launch.getDebugTarget().getThreads())
					if(t.canStepInto())
						t.stepInto();
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		//		showView(event);
	}

	static void stepOver(ExecutionEvent event) {
		if(launch != null) {
			try {
				for(IThread t : launch.getDebugTarget().getThreads())
					if(t.canStepOver())
						t.stepOver();
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		//		showView(event);
	}

	static void stepReturn(ExecutionEvent event) {
		if(launch != null) {
			try {
				for(IThread t : launch.getDebugTarget().getThreads())
					if(t.canStepReturn())
						t.stepReturn();
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		//		showView(event);
	}

	static void terminate(ExecutionEvent event) {
		if(launch != null) {
			try {
				IDebugTarget debugTarget = launch.getDebugTarget();
				if(debugTarget != null)
					debugTarget.terminate();
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		//		showView(event);
	}

//	private static void showView(ExecutionEvent event) {
//		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
//		try {
//			window.getActivePage().showView("pt.iscte.pandionj.view");
//		} catch (PartInitException e) {
//
//		}
//	}

}
