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


public class PandionJBreakpointListener implements IJavaBreakpointListener { 

	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
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
			pjView.handleExceptionBreakpoint(thread, ((IJavaExceptionBreakpoint) breakpoint).getExceptionTypeName());
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
