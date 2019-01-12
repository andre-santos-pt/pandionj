package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IReturn extends IStatement {
	IExpression getExpression();
	
	@Override
	default void execute(ICallStack callStack) throws ExecutionError {
		IValue value = callStack.evaluate(getExpression());
		callStack.getTopFrame().setReturn(value);
	}
}
