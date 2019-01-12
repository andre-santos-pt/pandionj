package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IVariableAssignment extends IStatement {
	IVariableDeclaration getVariable();
	IExpression getExpression();
	
	@Override
	default void execute(ICallStack callStack) throws ExecutionError {
		IValue value = callStack.evaluate(getExpression());
		callStack.getTopFrame().setVariable(getVariable().getIdentifier(), value);
	}
	/*
	boolean isAccumulation();
	*/
}
