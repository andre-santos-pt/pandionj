package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.operators.ArithmeticOperator;

public interface IVariableAssignment extends IStatement {
	IVariableDeclaration getVariable();
	IExpression getExpression();
	
	@Override
	default boolean isControl() {
		return false;
	}
	
	@Override
	default List<ISemanticProblem> validateSematics() {
		if(!getVariable().getType().equals(getExpression().getType()))
			return ImmutableList.of(ISemanticProblem.create("incompatible types", this, getExpression()));
		return ImmutableList.of();
	}
	
	@Override
	default boolean execute(ICallStack callStack) throws ExecutionError {
		IValue value = callStack.evaluate(getExpression());
		callStack.getTopFrame().setVariable(getVariable().getId(), value);
		return true;
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
