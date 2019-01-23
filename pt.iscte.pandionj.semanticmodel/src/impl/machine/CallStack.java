package impl.machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import model.machine.ICallStack;
import model.machine.IProgramState;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IProcedure;
import model.program.IProcedureDeclaration;

class CallStack implements ICallStack {

	private final ProgramState programState;
	private StackFrame[] stack;
	private int next;

	private List<ICallStack.IListener> listeners = new ArrayList<>(5);
	public void addListener(IListener listener) {
		listeners.add(listener);
	}
	
	public CallStack(ProgramState programState) {
		this.programState = programState;
		stack = new StackFrame[programState.getCallStackMaximum()];
		next = 0;
	}
	
	@Override
	public boolean isEmpty() {
		return next == 0;
	}
	
	@Override
	public int getSize() {
		return next;
	}
	
	@Override
	public IProgramState getProgramState() {
		return programState;
	}
	
	@Override
	public List<IStackFrame> getFrames() {
		return Collections.unmodifiableList(Arrays.asList(stack));
	}

	@Override
	public StackFrame getTopFrame() {
		if(getSize() == 0)
			throw new RuntimeException("empty call stack");
		return stack[next-1];
	}

	@Override
	public IStackFrame getLastTerminatedFrame() {
		assert stack[next] != null;
		return stack[next];
	}
	
	@Override
	public IStackFrame newFrame(IProcedure procedure, List<IValue> args) {
		if(next == stack.length)
			throw new RuntimeException("stack overflow");
		
		StackFrame parent = getSize() == 0 ? null : getTopFrame();
		StackFrame newFrame = new StackFrame(this, parent, procedure, args); 
		stack[next] = newFrame;
		next++;
		
		for(IListener l : listeners)
			l.stackFrameCreated(newFrame);
		
		return newFrame;
	}

	@Override
	public void terminateTopFrame(IValue returnValue) {
		next--;
		for(IListener l : listeners)
			l.stackFrameTerminated(stack[next], returnValue);
	}
	
	public void stepIn() throws ExecutionError {
		assert !isEmpty();
		stack[next-1].stepIn();
	}
	
	@Override
	public String toString() {
		String text = "";
		for(int i = 0; i < next; i++) {
			if(i != 0)
				text += " -> ";
			text += stack[i].getProcedure().getId() + "()";
		}
		return text;
	}

	
}
