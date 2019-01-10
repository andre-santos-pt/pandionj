package model.program.impl;

import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IBinaryOperator;

class BinaryExpression extends SourceElement implements IBinaryExpression {

	private final IBinaryOperator operator;
	private final IExpression left;
	private final IExpression right;
	
	
	public BinaryExpression(IBinaryOperator operator, IExpression left, IExpression right) {
		assert operator != null;
		assert left != null;
		assert right != null;
//		assert operator.isNumeric() && left.getType().isNumeric() && right.getType().isNumeric();
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	@Override
	public IValue evaluate(IStackFrame frame) {
		IValue leftValue = left.evaluate(frame);
		IValue rightValue = right.evaluate(frame);
		return operator.apply(leftValue, rightValue);
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
		return left + " " + operator + " " + right;
	}
	
	@Override
	public boolean isBoolean() {
		return 
				left.getType().getIdentifier().equals("boolean") && 
				right.getType().getIdentifier().equals("boolean");
	}
	
	
}
