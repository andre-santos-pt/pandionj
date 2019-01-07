package model.program;

import model.machine.ICallStack;

public interface IVariableAssignment extends IStatement {
	IVariableDeclaration getVariable();
	IExpression getExpression();
	
	@Override
	default void execute(ICallStack callStack) {
		callStack.getTopFrame().setVariable(getVariable().getIdentifier(), getExpression().evaluate(callStack.getTopFrame()));
	}
	/*
	boolean isAccumulation();
	*/
}
