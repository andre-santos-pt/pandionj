package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;



public class CallStackModel extends Observable {
	private List<StackFrameModel> stack;
	private int count;
	private boolean terminated;
	
	public CallStackModel() {
		stack = new ArrayList<>();
		count = 0;
		terminated = false;
	}

	public void update(IStackFrame[] stackFrames) {
		terminated = false;
		handle(stackFrames);
		for(int i = 0; i < count; i++)
			stack.get(i).update();
	}
	
	private void handle(IStackFrame[] stackFrames) {
		assert stackFrames != null;

		// TODO erro?
//		for(IStackFrame f : stackFrames) {
//			if(!(f.getLaunch().getSourceLocator().getSourceElement(f) instanceof IFile)) {
//				return 0;
//			}
//		}

		IStackFrame[] revStackFrames = reverse(stackFrames);
		count = revStackFrames.length;
		if(isSubStack(revStackFrames)) {
			for(int i = count; i < stack.size(); i++)
				stack.get(i).setObsolete();
		}
		else if(isStackIncrement(revStackFrames)) {
			for(int i = stack.size(); i < revStackFrames.length; i++)
				stack.add(new StackFrameModel(this, (IJavaStackFrame) revStackFrames[i]));
			setChanged();
		}
		else {
			stack.clear();
			for(int i = 0; i < revStackFrames.length; i++)
				stack.add(new StackFrameModel(this, (IJavaStackFrame) revStackFrames[i]));
			setChanged();
		}
		notifyObservers();
	}
	
	private boolean isSubStack(IStackFrame[] stackFrames) {
		if(stackFrames.length > stack.size())
			return false;
		
		for(int i = 0; i < stackFrames.length; i++)
			if(stackFrames[i] != stack.get(i).getStackFrame())
				return false;
		
		return true;
	}
	
	private boolean isStackIncrement(IStackFrame[] stackFrames) {
		if(stackFrames.length < stack.size())
			return false;
		
		for(int i = 0; i < stack.size(); i++)
			if(stackFrames[i] != stack.get(i).getStackFrame())
				return false;
		
		return true;
	}

	private static IStackFrame[] reverse(IStackFrame[] stackFrames) {
		IStackFrame[] revStackFrames = new IStackFrame[stackFrames.length];
		for(int i = 0; i < revStackFrames.length; i++)
			revStackFrames[i] = stackFrames[stackFrames.length-1-i];
		return revStackFrames;
	}

	public boolean isEmpty() {
		return count == 0;
	}

	public StackFrameModel getTopFrame() {
		assert !isEmpty();
		return stack.get(count-1);
	}

	public int getSize() {
		return count;
	}

	public List<StackFrameModel> getStackPath() {
		return Collections.unmodifiableList(stack);
	}

	public List<StackFrameModel> getFilteredStackPath() {
		return stack.stream().filter((f) -> f.getLineNumber() != -1).collect(Collectors.toList());
	}
	
	public void simulateGC() {
		for(int i = 0; i < count; i++)
			stack.get(i).simulateGC();
	}

	public StackFrameModel getFrame(IStackFrame exceptionFrame) {
		for(StackFrameModel s : stack)
			if(s.getStackFrame() == exceptionFrame)
				return s;
		
		assert false;
		return null;
	}

	public void setTerminated() {
		terminated = true;
	}
	
	public boolean isTerminated() {
		return terminated;
	}

//	public List<VariableModel<?>> getStaticVariables() {
//		return getTopFrame().getStaticVariables();
//	}
	
	private boolean sameLocation(Location loc, StackFrameModel frame) {
		try {
			return loc.declaringType().name().equals(frame.getStackFrame().getDeclaringTypeName()) &&
					loc.lineNumber() == frame.getLineNumber();
		} catch (DebugException e) {
			e.printStackTrace();
			return false;
		}
	}
	public void setReturnValue(List<StackFrame> frames, Value returnValue) {
		if(frames.size() == count) {
			for(int i = 0; i < frames.size(); i++) {
				if(!sameLocation(frames.get(i).location(), stack.get(count-1-i)))
					return;
			
			}
			stack.get(count-1).setReturnValue(returnValue);
		}
	}
}
