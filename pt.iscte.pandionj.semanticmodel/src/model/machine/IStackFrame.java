package model.machine;

import java.util.List;
import java.util.Map;

import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IStatement;

public interface IStackFrame {
	IStackFrame getParent(); // only null on root
	ICallStack getCallStack();
	
	IProcedure getProcedure();
	
	default boolean isRoot() {
		return getParent() == null;
	}
	
	Map<String, IValue> getVariables();
	
	IValue getVariable(String id);
	void addVariable(String identifier, IDataType type);
	void setVariable(String identifier, IValue value);
	
	IValue getReturn();
	void setReturn(IValue value);
	
	int getMemory();
	
	IStackFrame newChildFrame(IProcedure procedure, List<IValue> args);
	
	void terminateFrame(IValue returnValue);
	
	IValue getValue(String literal);
	IValue getValue(Object object);

	
	void execute(IStatement statement);
	void evaluate(IExpression expression);
	
}
