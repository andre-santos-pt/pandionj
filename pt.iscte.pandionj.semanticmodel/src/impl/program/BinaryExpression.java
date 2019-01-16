package impl.program;

import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IExpression;
import model.program.operators.IBinaryOperator;

class BinaryExpression extends SourceElement implements IBinaryExpression {
	private final IBinaryOperator operator;
	private final IExpression left;
	private final IExpression right;
	
	public BinaryExpression(IBinaryOperator operator, IExpression left, IExpression right) {
		assert operator != null;
		assert left != null;
		assert right != null;
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	@Override
	public IBinaryOperator getOperator() {
		return operator;
	}

	@Override
	public IExpression getLeftExpression() {
		return left;
	}

	@Override
	public IExpression getRightExpression() {
		return right;
	}
	
	@Override
	public IDataType getType() {
		return operator.getResultType(left, right);
	}

	@Override
	public String toString() {
		String l = left.toString();
		if(left instanceof IBinaryExpression)
			l = "(" + l + ")";
		
		String r = right.toString();
		if(right instanceof IBinaryExpression)
			r = "(" + r + ")";
		return l + " " + operator + " " + r;
	}
}
