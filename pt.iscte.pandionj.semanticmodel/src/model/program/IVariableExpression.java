package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IVariableExpression extends IExpression {
	IVariableDeclaration getVariable();
	
	@Override
	default IDataType getType() {
		return getVariable().getType();
	}

	@Override
	default IValue evaluate(ICallStack stack) throws ExecutionError {
		return stack.getTopFrame().getVariable(getVariable().getId());
	}
}
