package model.program;

import model.machine.IStackFrame;

public interface IReturn extends IExecutable {
	IExpression getExpression();
	
	@Override
	default void execute(IStackFrame stackFrame) {
		stackFrame.setReturn(getExpression().evaluate(stackFrame));
	}
}
