package impl.program;


import java.util.List;

import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IUnaryExpression;
import model.program.IUnaryOperator;

class UnaryExpression extends Expression implements IUnaryExpression {

	private final IUnaryOperator operator;
	private final IExpression expression;
	
	public UnaryExpression(IUnaryOperator operator, IExpression expression) {
		assert operator != null;
		assert expression != null;
		this.operator = operator;
		this.expression = expression;
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
	public boolean isDecomposable() {
		return true;
	}	
	
	@Override
	public String toString() {
		return operator.getSymbol() + "(" + expression + ")";
	}

	@Override
	public IValue evalutate(List<IValue> values, ICallStack stack) {
		assert values.size() == 1;
		return getOperator().apply(values.get(0));
	}
}
