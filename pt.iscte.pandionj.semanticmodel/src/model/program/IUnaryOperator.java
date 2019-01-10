package model.program;

import model.machine.IValue;

public interface IUnaryOperator extends IOperator {

	
	// pre: isUnary()
	default IDataType getResultType(IExpression exp) {
		assert false;
		return null;
	}
	
	// pre: isUnary()
	default IValue apply(IValue value) {
		assert false;
		return null;
	}
}
