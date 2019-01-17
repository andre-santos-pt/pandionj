package model.program;

import model.machine.ICallStack;
import model.machine.IStructObject;
import model.machine.IValue;

public interface IStructMemberAssignment extends IStatement {

	IVariableDeclaration getVariable();
	String getMemberId();
	IExpression getExpression();

	@Override
	default boolean isControl() {
		return false;
	}
	
	@Override
	default boolean execute(ICallStack stack) throws ExecutionError {
		IStructObject object = (IStructObject) stack.getTopFrame().getVariable(getVariable().getId());
		IValue val = stack.evaluate(getExpression());
		object.setField(getMemberId(), val);
		return true;
	}
	
}
