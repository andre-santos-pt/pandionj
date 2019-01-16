package model.program;

import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IStructObject;

public interface IStructAllocation extends IExpression {
	
	IStructType getStructType();
	
	@Override
	default IStructObject evaluate(ICallStack stack) throws ExecutionError {
		IStackFrame frame = stack.getTopFrame();
		IStructObject array = frame.getObject(getStructType()); 
		return array;
	}
}
