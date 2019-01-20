package impl.program;

import java.util.List;

import impl.machine.ExecutionError;
import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;

class VariableExpression extends Expression implements IVariableExpression {

	private final IVariableDeclaration variable;
	
	public VariableExpression(IVariableDeclaration variable) {
		this.variable = variable;
	}

	@Override
	public IVariableDeclaration getVariable() {
		return variable;
	}

	@Override
	public String toString() {
		return variable.getId();
	}
	
	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) throws ExecutionError {
		return stack.getTopFrame().getVariable(getVariable().getId());
	}
}
