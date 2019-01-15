package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IBinaryExpression extends IExpression {
	IBinaryOperator getOperator();
	IExpression getLeftExpression();
	IExpression getRightExpression();
	
	default boolean isOperation() {
		return true;
	}
	
	
	@Override
	default public IValue evaluate(ICallStack stack) throws ExecutionError {
		return getOperator().apply(getLeftExpression(), getRightExpression(), stack.getTopFrame());
	}
}

