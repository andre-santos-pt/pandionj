package model.machine;

import java.util.List;
import java.util.Map;

import model.program.ExecutionError;
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
	
	void terminateFrame();
	
	IValue getValue(String literal);
	IValue getValue(Object object);

	IArray getArray(IDataType baseType, int length);
	
	void execute(IStatement statement) throws ExecutionError;
	
	IValue evaluate(IExpression expression) throws ExecutionError;
	
	void addListener(IListener listener);
	
	interface IListener {
		default void variableAdded(String identifier, IDataType type) { }
		
		default void variableModified(String identifier, IDataType type, IValue newValue) { }
		
		default void statementExecutionStart(IStatement statement) { }

		default void statementExecutionEnd(IStatement statement) { }

		default void expressionEvaluationStart(IExpression expression) { }

		default void expressionEvaluationEnd(IExpression expression, IValue result) { }
		
		default void started(IValue result) { }
		
		default void terminated(IValue result) { }
	}
	
}
