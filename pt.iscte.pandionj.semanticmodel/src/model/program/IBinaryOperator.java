package model.program;

import impl.machine.ExecutionError;
import model.machine.IValue;

public interface IBinaryOperator extends IOperator {
	IDataType getResultType(IExpression left, IExpression right);
	
	IValue apply(IValue left, IValue right) throws ExecutionError;
}
