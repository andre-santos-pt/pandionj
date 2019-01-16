package model.program.operators;

import model.machine.IStackFrame;
import model.machine.IValue;
import model.program.ExecutionError;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IExpression.OperationType;

public enum LogicalOperator implements IBinaryOperator {
	AND("&&") {
		@Override
		public IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError {
			IValue leftValue =  frame.evaluate(left);
			if(leftValue == IValue.FALSE)
				return IValue.FALSE;
			
			IValue rightValue = frame.evaluate(right);
			return IValue.booleanValue(rightValue == IValue.TRUE);
		}
	},
	OR("||") {
		@Override
		public IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError {
			IValue leftValue =  frame.evaluate(left);
			if(leftValue == IValue.TRUE)
				return IValue.TRUE;
			IValue rightValue = frame.evaluate(right);
			return IValue.booleanValue(rightValue == IValue.TRUE);
		}
	}, 
	XOR("^") {
		@Override
		public IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError {
			IValue leftValue =  frame.evaluate(left);
			IValue rightValue = frame.evaluate(right);
			return IValue.booleanValue(leftValue != rightValue);
		}
	};

	private String symbol;

	private LogicalOperator(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	public IDataType getResultType(IExpression left, IExpression right) {
		return IDataType.BOOLEAN;
	}
	
	@Override
	public OperationType getOperationType() {
		return OperationType.LOGICAL;
	}
}
