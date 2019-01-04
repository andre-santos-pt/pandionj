package model.machine;

import java.util.Map;

import model.program.IVariableDeclaration;

public interface IStackFrame {
	IStackFrame getParent(); // only null on root
	ICallStack getCallStack();
	
	default boolean isRoot() {
		return getParent() == null;
	}
	
	Map<String, IValue> getVariables();
	
	IValue getVariable(String name);
	
	IValue getReturn();
	void setReturn(IValue value);
	
	int getMemory();
	
	IStackFrame newChildFrame(Map<IVariableDeclaration, IValue> variables);
	void terminateFrame(IValue returnValue);
	
	IValue getValue(String literal);
}
