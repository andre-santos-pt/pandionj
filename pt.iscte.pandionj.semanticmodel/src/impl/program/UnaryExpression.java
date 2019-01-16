package impl.program;


import model.program.IDataType;
import model.program.IExpression;
import model.program.IUnaryExpression;
import model.program.operators.IUnaryOperator;

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
