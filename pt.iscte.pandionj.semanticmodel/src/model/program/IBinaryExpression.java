package model.program;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface IBinaryExpression extends IExpression {
	IBinaryOperator getOperator();
	IExpression getLeftExpression();
	IExpression getRightExpression();
	
	default boolean isOperation() {
		return true;
	}
	
	
	@Override
	default public IValue evaluate(IStackFrame frame) throws ExecutionError {
		return getOperator().apply(getLeftExpression(), getRightExpression(), frame);
	}
}

