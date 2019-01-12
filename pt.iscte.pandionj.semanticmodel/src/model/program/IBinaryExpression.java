package model.program;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface IBinaryExpression extends IExpression {
	IBinaryOperator getOperator();
	IExpression getLeftExpression();
	IExpression getRightExpression();
	
	boolean isBoolean();
	
	default boolean isOperation() {
		return true;
	}
	
	
	@Override
	default public IValue evaluate(IStackFrame frame) throws ExecutionError {
		IValue leftValue =  frame.evaluate(getLeftExpression());
		IValue rightValue = frame.evaluate(getRightExpression());
		return getOperator().apply(leftValue, rightValue);
	}
}

