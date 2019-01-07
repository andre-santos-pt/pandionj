package model.program;

import model.machine.ICallStack;

public interface IReturn extends IStatement {
	IExpression getExpression();
	
	@Override
	default void execute(ICallStack callStack) {
		callStack.getTopFrame().setReturn(getExpression().evaluate(callStack.getTopFrame()));
	}
}
