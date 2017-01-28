package pt.iscte.pandionj;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "pt.iscte.pandionj2"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private String lastExceptionClassName = null;

	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
//		IDebugContextListener listener = new IDebugContextListener() {
//			
//			@Override
//			public void debugContextChanged(DebugContextEvent event) {
//				ISelection context = event.getContext();
//				
//				if (context instanceof StructuredSelection) {
//					Object data = ((StructuredSelection) context).getFirstElement();
//					if (data instanceof IStackFrame) {
//						IStackFrame stackFrame = (IStackFrame) data;
//						try {
//							IStackFrame[] frames = stackFrame.getThread().getStackFrames();
//							RealView.redrawStack(frames);
//						} catch (DebugException e) {
////							RealView.clear();
//						}
//					} 
//				}
//				
//			}
//		};
//		DebugUITools.getDebugContextManager().addDebugContextListener(listener);
//		
		JDIDebugModel.addJavaBreakpointListener(new IJavaBreakpointListener() {
			
            public int installingBreakpoint(IJavaDebugTarget target,
                    IJavaBreakpoint breakpoint, IJavaType type) { return 0; }

            public void breakpointRemoved(IJavaDebugTarget target,
                    IJavaBreakpoint breakpoint) { }

            public void breakpointInstalled(IJavaDebugTarget target,
                    IJavaBreakpoint breakpoint) { }

            public int breakpointHit(IJavaThread thread,
                    IJavaBreakpoint breakpoint) {
                if (breakpoint instanceof IJavaExceptionBreakpoint) {
                    IJavaExceptionBreakpoint exc = (IJavaExceptionBreakpoint) breakpoint;
                    lastExceptionClassName = exc.getExceptionTypeName();
                    System.out.println("EXC:" + lastExceptionClassName);
                }
                return 0;
            }

            public void breakpointHasRuntimeException(
                    IJavaLineBreakpoint breakpoint, DebugException exception) { }

            public void addingBreakpoint(IJavaDebugTarget target,
                    IJavaBreakpoint breakpoint) { }

            public void breakpointHasCompilationErrors(
                    IJavaLineBreakpoint breakpoint, Message[] errors) { }
        });
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
