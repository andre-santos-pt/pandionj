package model.program.operators;

import impl.machine.ExecutionError;
import model.machine.IValue;
import model.program.IBinaryOperator;
import model.program.IDataType;
import model.program.IExpression;

// TODO step eval
public enum LogicalOperator implements IBinaryOperator {
	AND("&&") {
//		@Override
//		public IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError {
//			IValue leftValue =  frame.evaluate(left);
//			if(leftValue == IValue.FALSE)
//				return IValue.FALSE;
//			
//			IValue rightValue = frame.evaluate(right);
//			return IValue.booleanValue(rightValue == IValue.TRUE);
//		}
		
		@Override
		public IValue apply(IValue left, IValue right) throws ExecutionError {
			return IValue.booleanValue(left == IValue.TRUE && right == IValue.TRUE);
		}
	},
	OR("||") {
//		@Override
//		public IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError {
//			IValue leftValue =  frame.evaluate(left);
//			if(leftValue == IValue.TRUE)
//				return IValue.TRUE;
//			IValue rightValue = frame.evaluate(right);
//			return IValue.booleanValue(rightValue == IValue.TRUE);
//		}
		@Override
		public IValue apply(IValue left, IValue right) throws ExecutionError {
			return IValue.booleanValue(left == IValue.TRUE || right == IValue.TRUE);
		}
	}, 
	XOR("^") {
//		@Override
//		public IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError {
//			IValue leftValue =  frame.evaluate(left);
//			IValue rightValue = frame.evaluate(right);
//			return IValue.booleanValue(leftValue != rightValue);
//		}
		@Override
		public IValue apply(IValue left, IValue right) throws ExecutionError {
			return IValue.booleanValue(left != right);
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
