package model.program;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface IVariableExpression extends IExpression {
	IVariableDeclaration getVariable();
	
	default boolean isOperation() {
		return false;
	}
	
	
	@Override
	default IDataType getType() {
		return getVariable().getType();
	}

	@Override
	default IValue evaluate(IStackFrame frame) throws ExecutionError {
		return frame.getVariable(getVariable().getIdentifier());
	}
}
