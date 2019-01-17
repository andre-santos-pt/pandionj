package model.program;

import model.machine.ICallStack;
import model.machine.IStructObject;

public interface IStructAllocation extends IExpression {
	
	@Override
	IStructType getType();
	
	@Override
	default IStructObject evaluate(ICallStack stack) throws ExecutionError {
		return stack.getTopFrame().getObject(getType());
	}
}
