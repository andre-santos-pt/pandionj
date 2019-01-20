package impl.program;

import java.util.List;

import impl.machine.ExecutionError;
import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IExpression;
import model.program.IReturn;

class Return extends Statement implements IReturn {

	private final IExpression expression;
	
	public Return(Block parent, IExpression expression) {
		super(parent, true);
		this.expression = expression;
	}

	@Override
	public IExpression getExpression() {
		return expression;
	}
	
	@Override
	public String toString() {
		return "return " + expression;
	}
	
	@Override
	public boolean execute(ICallStack stack, List<IValue> expressions) throws ExecutionError {
		if(expressions.size() == 1)
			stack.getTopFrame().setReturn(expressions.get(0));
		return false;
	}
}
