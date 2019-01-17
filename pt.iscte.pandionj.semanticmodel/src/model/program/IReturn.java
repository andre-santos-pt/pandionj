package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IReturn extends IStatement {
	IExpression getExpression(); // may be null (void)
	
	@Override
	default boolean isControl() {
		return false;
	}
	
	default IDataType getReturnValueType() {
		return getExpression() == null ? IDataType.VOID : getExpression().getType();
	}
	
	@Override
	default boolean execute(ICallStack callStack) throws ExecutionError {
		IExpression expression = getExpression();
		if(expression != null) {
			IValue value = callStack.evaluate(getExpression());
			callStack.getTopFrame().setReturn(value);
		}
		return false;
	}
}
