package model.program;

import model.machine.IValue;

public interface IBinaryOperator extends IOperator {

	IDataType getResultType(IExpression left, IExpression right);

	IValue apply(IValue left, IValue right);
}
