package model.program;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.operators.IBinaryOperator;

public interface IBinaryExpression extends IExpression {
	IBinaryOperator getOperator();
	IExpression getLeftExpression();
	IExpression getRightExpression();
	
	@Override
	default OperationType getOperationType() {
		return getOperator().getOperationType();
	}
	
	
	@Override
	default public IValue evaluate(ICallStack stack) throws ExecutionError {
		return getOperator().apply(getLeftExpression(), getRightExpression(), stack.getTopFrame());
	}
}

