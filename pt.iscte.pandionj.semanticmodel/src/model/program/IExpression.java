package model.program;

import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IExpression extends ISourceElement {
	// ARCH: only called my stack frame
	IValue evaluate(ICallStack frame) throws ExecutionError;
	
	IDataType getType();
	
	boolean isOperation();
}
