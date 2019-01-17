package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IExpression extends ISourceElement {
	// ARCH: only called my stack frame
	IValue evaluate(ICallStack stack) throws ExecutionError;
	
	IDataType getType();
	
	// TODO concretize expression
	//String concretize();
	
	default OperationType getOperationType() {
		return OperationType.OTHER;
	}
	
	enum OperationType {
		ARITHMETIC, RELATIONAL, LOGICAL, CALL, OTHER;
	}
}
