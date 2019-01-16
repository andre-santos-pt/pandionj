package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface ILiteral extends IExpression {
	String getStringValue();
	
	@Override
	default IValue evaluate(ICallStack stack) {
		return stack.getProgramState().getValue(getStringValue());
	}
}
