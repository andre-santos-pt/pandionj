package model.program;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface IVariableExpression extends IExpression {
	IVariableDeclaration getVariable();
	
	@Override
	default IDataType getType() {
		return getVariable().getType();
	}

	@Override
	default IValue evaluate(IStackFrame frame) {
		return frame.getVariable(getVariable().getIdentifier());
	}
}
