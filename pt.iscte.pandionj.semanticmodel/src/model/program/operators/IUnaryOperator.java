package model.program.operators;

import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IOperator;

public interface IUnaryOperator extends IOperator {
	IDataType getResultType(IExpression exp);	
	IValue apply(IValue value);
}
