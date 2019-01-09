package model.program.impl;

import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IExpression;
import model.program.Operator;

class BinaryExpression extends SourceElement implements IBinaryExpression {

	private final Operator operator;
	private final IExpression left;
	private final IExpression right;
	
	
	public BinaryExpression(Operator operator, IExpression left, IExpression right) {
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
		int l = (int) left.evaluate(frame).getValue();
		int r = (int) right.evaluate(frame).getValue();
		if(operator == Operator.ADD) {
			return frame.getValue(l + r);
		}
		if(operator == Operator.GREATER) {
			return frame.getValue(l > r);
		}
		return null;
	}

	@Override
	public Operator getOperator() {
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
	public String toString() {
		return left + " " + operator + " " + right;
	}
	
	@Override
	public boolean isBoolean() {
		return 
				left.getType().getIdentifier().equals("boolean") && 
				right.getType().getIdentifier().equals("boolean");
	}
	
	@Override
	public IDataType getType() {
		// TODO Auto-generated method stub
		return null;
	}
}
