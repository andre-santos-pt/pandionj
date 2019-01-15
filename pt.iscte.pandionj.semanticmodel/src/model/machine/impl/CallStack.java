package model.machine.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import model.machine.ICallStack;
import model.machine.IProgramState;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IProcedure;

class CallStack implements ICallStack {

	private final ProgramState programState;
	private IStackFrame[] stack;
	private int next;

	private List<ICallStack.IListener> listeners = new ArrayList<>(5);
	public void addListener(IListener listener) {
		listeners.add(listener);
	}
	
	public CallStack(ProgramState programState) {
		this.programState = programState;
		stack = new IStackFrame[programState.getCallStackMaximum()];
		next = 0;
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
	public IStackFrame getTopFrame() {
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

		IStackFrame parent = getSize() == 0 ? null : getTopFrame();
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
		
//		IProcedure procedure = stack[next].getProcedure();
//		stack[next].terminateFrame();
//		System.out.println("/ " + procedure + " -> " + returnValue);

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
