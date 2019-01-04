package model.machine.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import model.machine.ICallStack;
import model.machine.IProgramState;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IVariableDeclaration;

public class CallStack implements ICallStack {

	private final ProgramState programState;
	private IStackFrame[] stack;
	private int next;

	public CallStack(ProgramState programState) {
		this.programState = programState;
		stack = new IStackFrame[1024];
		next = 0;
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
		return next == 0 ? null : stack[next-1];
	}

	@Override
	public IStackFrame newFrame(Map<IVariableDeclaration, IValue> variables) {
		if(next == stack.length)
			throw new RuntimeException("stack overflow");
		StackFrame newFrame = new StackFrame(this, getTopFrame(), variables); 
		stack[next] = newFrame;
		next++;
		return newFrame;
	}

	@Override
	public void terminateTopFrame(IValue returnValue) {
		next--;
	}
}
