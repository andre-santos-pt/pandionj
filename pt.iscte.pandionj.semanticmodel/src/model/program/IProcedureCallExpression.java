package model.program;

import java.util.List;

import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IProcedureCallExpression extends IExpression {
	IProcedure getProcedure();
	List<IExpression> getArguments();
	
	@Override
	default IValue evaluate(ICallStack stack) throws ExecutionError {
		IProcedureCall.executeCall(stack, getProcedure(), getArguments(), this);
		IStackFrame last = stack.getLastTerminatedFrame();
		return last.getReturn();
	}
}
