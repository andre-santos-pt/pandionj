package model.program;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.operators.IUnaryOperator;

public interface IUnaryExpression extends IExpression {
	IUnaryOperator getOperator();
	IExpression getExpression();
	
	@Override
	default public IValue evaluate(ICallStack stack) throws ExecutionError {
		IValue val = stack.getTopFrame().evaluate(getExpression());
		IValue result = getOperator().apply(val);
		return result;
	}

}
