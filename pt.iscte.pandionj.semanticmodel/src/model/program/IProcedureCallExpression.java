package model.program;

import java.util.List;

import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IProcedureCallExpression extends IExpression {
	IProcedure getProcedure();
	List<IExpression> getArgs();
	
	@Override
	default boolean isOperation() {
		return false;
	}

	@Override
	default IValue evaluate(ICallStack stack) throws ExecutionError {
		IProcedureCall.executeCall(stack, getProcedure(), getArgs(), this);
//		stackFrame.execute(getProcedure().callExpression(getArgs()));
		IStackFrame last = stack.getLastTerminatedFrame();
		return last.getReturn();
	}
}
