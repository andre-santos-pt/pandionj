package model.program;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface IExpression extends ISourceElement {
	// ARCH: only called my stack frame
	IValue evaluate(IStackFrame frame) throws ExecutionError;
	
	IDataType getType();
	
	boolean isOperation();
}
