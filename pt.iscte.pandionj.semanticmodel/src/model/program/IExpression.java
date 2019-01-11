package model.program;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface IExpression extends ISourceElement {
	IValue evaluate(IStackFrame frame) throws ExecutionError;
	IDataType getType();
}
