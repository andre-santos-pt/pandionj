package pt.iscte.pandionj;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.MethodExitRequestImpl;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.swt.widgets.Display;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodExitEvent;

import pt.iscte.pandionj.model.CallStackModel;


@SuppressWarnings("restriction")
public class PandionJBreakpointListener implements IJavaBreakpointListener, IJDIEventListener {

	@Override
	public boolean handleEvent(Event event, JDIDebugTarget target, boolean suspendVote, EventSet eventSet) {
		MethodExitEvent mevent = (MethodExitEvent) event;
//		Method method = mevent.method();
		
		// TODO sync with stack
//		if(!method.isSynthetic()) { //&& !method.name().equals("main")) {
			try {
				//System.out.println(uniqueID + " " + mevent.thread().frameCount() + " " + mevent.method().name() + "() = " + mevent.returnValue());
				
				model.setReturnValue( mevent.thread().frames(), mevent.returnValue());
				
//				System.out.println(mevent.location().);
			} catch (IncompatibleThreadStateException e) {
				e.printStackTrace();
			}
//		}
		return true;
	}
	
	@Override
	public void eventSetComplete(Event event, JDIDebugTarget target, boolean suspend, EventSet eventSet) {
		
	}

	private CallStackModel model;
	
	PandionJBreakpointListener(CallStackModel model) {
		this.model = model;
	}
	
	
	private JDIDebugTarget debugTarget;
	private MethodExitRequestImpl methodExitRequestImpl = null;
	
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		JDIThread t = (JDIThread) thread;
		JDIDebugTarget target = (JDIDebugTarget) t.getDebugTarget();
		
		if(target != debugTarget) {
			debugTarget = target;
			methodExitRequestImpl = new MethodExitRequestImpl((VirtualMachineImpl) target.getVM());
			methodExitRequestImpl.addClassFilter("Rec"); // TODO generic filter)
//			methodExitRequestImpl.addClassFilter("Test"); 
			methodExitRequestImpl.setEnabled(true);
			debugTarget.addJDIEventListener(this, methodExitRequestImpl);
		}
		
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
