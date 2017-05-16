package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame;



public class CallStackModel {
	private List<StackFrameModel> stack;

	public CallStackModel() {
		stack = new ArrayList<>();
	}
	
	public int handle(IStackFrame[] stackFrames) {
		assert stackFrames != null;

		// TODO erro?
		for(IStackFrame f : stackFrames) {
			if(!(f.getLaunch().getSourceLocator().getSourceElement(f) instanceof IFile)) {
				return 0;
			}
		}

		IStackFrame[] revStackFrames = new IStackFrame[stackFrames.length];
		for(int i = 0; i < revStackFrames.length; i++)
			revStackFrames[i] = stackFrames[stackFrames.length-1-i];
		
		int base = -1;
		for(int i = 0; i < revStackFrames.length; i++) {
			if(i < stack.size() && stack.get(i).getStackFrame() == revStackFrames[i])
				base = i;
			else {
				while(stack.size() > i)
					stack.remove(i);
				stack.add(new StackFrameModel((IJavaStackFrame) revStackFrames[i]));	
			}
		}
		
		while(stack.size() > revStackFrames.length)
			stack.remove(revStackFrames.length);
		return base;
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public StackFrameModel getTopFrame() {
		assert !isEmpty();
		return stack.get(stack.size()-1);
	}

	public int getSize() {
		return stack.size();
	}

	public void update(int step) {
		for(StackFrameModel m : stack)
			m.update();
	}

	public List<StackFrameModel> getStackPath() {
		return Collections.unmodifiableList(stack);
	}

	public void simulateGC() {
		for(StackFrameModel m : stack)
			m.simulateGC();
	}
}
