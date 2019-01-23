package model.machine;

import java.util.List;
import java.util.Map;

import impl.machine.ExecutionError;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IStatement;
import model.program.IStructType;
import model.program.IVariableDeclaration;

public interface IStackFrame {
//	IStackFrame getParent(); // only null on root
	ICallStack getCallStack();
	
	IProcedure getProcedure();
	
	Map<String, IValue> getVariables();
	
	IValue getVariableValue(String id);
	void setVariable(String identifier, IValue value);
	
	// TODO timestamp / history

	
	IValue getReturn();
	void setReturn(IValue value);
	
	default int getMemory() {
		int bytes = 0;
		for (IVariableDeclaration var : getProcedure().getVariables()) {
			bytes += var.getType().getMemoryBytes();
		}
		return bytes;
	}
	
	IStackFrame newChildFrame(IProcedure procedure, List<IValue> args);
	
	void terminateFrame();
	
	IValue getValue(String literal);
	IValue getValue(Object object);

	IArray allocateArray(IDataType baseType, int[] dimensions);
	
	IStructObject allocateObject(IStructType type);
	
	IValue evaluate(IExpression expression, List<IValue> expressions) throws ExecutionError;
	
	
	void stepIn() throws ExecutionError;
	
	boolean isOver();
	
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
