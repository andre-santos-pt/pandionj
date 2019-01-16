package model.program.operators;

import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.ExecutionError;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IOperator;
import model.program.IExpression.OperationType;

public interface IBinaryOperator extends IOperator {

	IDataType getResultType(IExpression left, IExpression right);

	IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError;
	
	IExpression.OperationType getOperationType();
}
