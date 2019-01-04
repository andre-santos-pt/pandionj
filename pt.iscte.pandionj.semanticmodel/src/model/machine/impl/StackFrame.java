package model.machine.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IVariableDeclaration;

public class StackFrame implements IStackFrame {

	private final ICallStack callStack;
	private final IStackFrame parent;
	private final Map<String, IValue> variables;
	private IValue returnValue;
	
	public StackFrame(ICallStack callStack, IStackFrame parent, Map<IVariableDeclaration, IValue> arguments) {
		this.callStack = callStack;
		this.parent = parent;
		this.variables = new HashMap<>();
		for(Entry<IVariableDeclaration, IValue> e : arguments.entrySet()) {
			variables.put(e.getKey().getIdentifier(), e.getValue());
		}
	}
	
	@Override
	public IStackFrame getParent() {
		return parent;
	}

	@Override
	public Map<String, IValue> getVariables() {
		return Collections.unmodifiableMap(variables);
	}

	@Override
	public IValue getVariable(String name) {
		return variables.get(name);
	}

	@Override
	public IValue getReturn() {
		return returnValue;
	}
	
	@Override
	public void setReturn(IValue value) {
		this.returnValue = value;
	}
	
	@Override
	public int getMemory() {
		return 0;
	}

	@Override
	public ICallStack getCallStack() {
		return callStack;
	}

	@Override
	public IStackFrame newChildFrame(Map<IVariableDeclaration, IValue> variables) {
		return callStack.newFrame(variables);
	}

	@Override
	public void terminateFrame(IValue returnValue) {
		callStack.terminateTopFrame(returnValue);
	}
	
	@Override
	public IValue getValue(String literal) {
		return callStack.getProgramState().getValue(literal);
	}
}
