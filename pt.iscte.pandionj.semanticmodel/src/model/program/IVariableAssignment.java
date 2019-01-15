package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface IVariableAssignment extends IStatement {
	IVariableDeclaration getVariable();
	IExpression getExpression();
	
	@Override
	default void execute(ICallStack callStack) throws ExecutionError {
		IValue value = callStack.evaluate(getExpression());
		callStack.getTopFrame().setVariable(getVariable().getId(), value);
	}
	
	default ArithmeticOperator getAccumulationOperator() {
		IExpression expression = getExpression();
		if(expression instanceof IBinaryExpression) {
			IBinaryExpression e = (IBinaryExpression) expression;
			IExpression left = e.getLeftExpression();
			IExpression right = e.getRightExpression();
			if(e.getOperator() instanceof ArithmeticOperator && 
				(
				left instanceof IVariableExpression && ((IVariableExpression) left).getVariable().equals(this.getVariable()) ||
				right instanceof IVariableExpression && ((IVariableExpression) right).getVariable().equals(this.getVariable()))
				)
				return (ArithmeticOperator) e.getOperator();
		}
		return null;
	}
	
	default boolean isAccumulation() {
		return getAccumulationOperator() != null;
	}
	
}
