package model.program.impl;


import model.machine.ICallStack;
import model.machine.IValue;
import model.program.ExecutionError;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IUnaryExpression;
import model.program.IUnaryOperator;

public class UnaryExpression extends SourceElement implements IUnaryExpression {

	private final IUnaryOperator operator;
	private final IExpression expression;
	
	public UnaryExpression(IUnaryOperator operator, IExpression expression) {
		assert operator != null;
		assert expression != null;
		this.operator = operator;
		this.expression = expression;
	}

	@Override
	public IValue evaluate(ICallStack stack) throws ExecutionError {
		IValue val = stack.getTopFrame().evaluate(expression);
		IValue result = operator.apply(val);
		return result;
	}

	@Override
	public IDataType getType() {
		return operator.getResultType(expression);
	}

	@Override
	public IUnaryOperator getOperator() {
		return operator;
	}

	@Override
	public IExpression getExpression() {
		return expression;
	}
	
	@Override
	public String toString() {
		return operator.getSymbol() + "(" + expression + ")";
	}

}
