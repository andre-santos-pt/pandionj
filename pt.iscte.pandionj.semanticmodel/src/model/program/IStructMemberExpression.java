package model.program;

import model.machine.ICallStack;
import model.machine.IStructObject;
import model.machine.IValue;

public interface IStructMemberExpression extends IExpression {

	IVariableDeclaration getVariable();
	String getMemberId();
	
	@Override
	default IValue evaluate(ICallStack stack) throws ExecutionError {
		// TODO validate
		IStructObject object = (IStructObject) stack.getTopFrame().getVariable(getVariable().getId());
		IValue field = object.getField(getMemberId());
		return field;
	}
	
}
