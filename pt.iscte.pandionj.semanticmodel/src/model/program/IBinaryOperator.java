package model.program;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface IBinaryOperator extends IOperator {

	IDataType getResultType(IExpression left, IExpression right);

	IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError;
	
	IExpression.OperationType getOperationType();
}
