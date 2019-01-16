package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IConstantExpression extends IExpression {

	IConstantDeclaration getConstant();
	
	@Override
	default public IValue evaluate(ICallStack frame) throws ExecutionError {
		return frame.evaluate(getConstant().getValue());
	}
}
