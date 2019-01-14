package model.program;

import model.machine.IValue;

public interface IUnaryOperator extends IOperator {
	IDataType getResultType(IExpression exp);	
	IValue apply(IValue value);
}
