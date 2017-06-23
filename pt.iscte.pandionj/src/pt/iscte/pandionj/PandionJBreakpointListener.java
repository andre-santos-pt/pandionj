package pt.iscte.pandionj;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;


public class PandionJBreakpointListener implements IJavaBreakpointListener { // IJDIEventListener {

//	@Override
//	public boolean handleEvent(Event event, JDIDebugTarget target, boolean suspendVote, EventSet eventSet) {
//		MethodExitEvent mevent = (MethodExitEvent) event;
//		//		Method method = mevent.method();
//
//		// TODO sync with stack
//		//		if(!method.isSynthetic()) { //&& !method.name().equals("main")) {
//		try {
//			//System.out.println(uniqueID + " " + mevent.thread().frameCount() + " " + mevent.method().name() + "() = " + mevent.returnValue());
//
//			model.setReturnValue( mevent.thread().frames(), mevent.returnValue());
//
//			//				System.out.println(mevent.location().);
//		} catch (IncompatibleThreadStateException e) {
//			e.printStackTrace();
//		}
//		//		}
//		return true;
//	}
//
//	@Override
//	public void eventSetComplete(Event event, JDIDebugTarget target, boolean suspend, EventSet eventSet) {
//
//	}
//
//	private JDIDebugTarget debugTarget;
//	private MethodExitRequestImpl request = null;

//	private RuntimeModel model;
//
//	PandionJBreakpointListener(RuntimeModel model) {
//		this.model = model;
//	}

//	public void enableFilter() {
//		if(request != null) {
//			try {
//				request.setEnabled(true);
//			}
//			catch(VMDisconnectedException e) {
//
//			}
//		}
//	}
//
//	public void disableFilter() {
//		if(request != null) {
//			try {
//				request.setEnabled(false);
//			}
//			catch(VMDisconnectedException e) {
//
//			}
//		}
//	}


	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
//		JDIThread t = (JDIThread) thread;
//		JDIDebugTarget target = (JDIDebugTarget) t.getDebugTarget();
//
//		if(target != debugTarget) {
//			if(request != null)
//				debugTarget.removeJDIEventListener(this, request);
//
//			IFile sourceElement = PandionJUI.execute(() -> {
//				return (IFile) thread.getLaunch().getSourceLocator().getSourceElement(thread.getTopStackFrame());
//			}, null);
//
//			//			try {
//			//				sourceElement = (IFile) thread.getLaunch().getSourceLocator().getSourceElement(thread.getTopStackFrame());
//			//			} catch (DebugException e) {
//			//				e.printStackTrace();
//			//			}
//			debugTarget = target;
//			if(sourceElement != null) {
//				request = new MethodExitRequestImpl((VirtualMachineImpl) target.getVM());
//				request.addClassFilter(sourceElement.getName().substring(0, sourceElement.getName().indexOf('.'))); // TODO class name problem
//				//				request.addSourceNameFilter(sourceElement.getName() + "*");
//				//			methodExitRequestImpl.addClassFilter("Test"); 
//				request.setEnabled(true);
//
//				// TODO temp
////				debugTarget.addJDIEventListener(this, request);
//			}
//		}

		PandionJView pjView = PandionJView.getInstance();
		if(pjView == null)
			return IJavaBreakpointListener.DONT_SUSPEND;

		if(breakpoint instanceof IJavaLineBreakpoint) {
			if(!thread.isPerformingEvaluation()) {
				pjView.handleLinebreakPoint(thread);
				return IJavaBreakpointListener.SUSPEND;
			}
		}
		else if (breakpoint instanceof IJavaExceptionBreakpoint) {
			pjView.handleExceptionBreakpoint(thread, (IJavaExceptionBreakpoint) breakpoint);
			return IJavaBreakpointListener.SUSPEND;
		}
		return IJavaBreakpointListener.DONT_SUSPEND;
	}

	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		return IJavaBreakpointListener.DONT_CARE;
	}

	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) { }
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) { }
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) { }
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) { }
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) { }



}
