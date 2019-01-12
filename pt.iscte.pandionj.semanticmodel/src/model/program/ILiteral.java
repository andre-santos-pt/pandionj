package model.program;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface ILiteral extends IExpression {
	String getStringValue();
	
	default boolean isOperation() {
		return true;
	}
	
	@Override
	default IValue evaluate(IStackFrame frame) {
		return frame.getValue(getStringValue());
	}
}
