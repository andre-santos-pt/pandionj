package model.program;

import java.math.BigDecimal;
import java.util.function.BiFunction;

import model.machine.IStackFrame;
import model.machine.IValue;
import model.machine.impl.Value;

enum ArithmeticOperator implements IBinaryOperator {
	ADD("+") {
		protected BigDecimal calculate(IValue left, IValue right) {
			return ((BigDecimal)left.getValue()).add((BigDecimal) right.getValue());
		}
	}, 
	SUB("-") {
		protected BigDecimal calculate(IValue left, IValue right) {
			return ((BigDecimal)left.getValue()).subtract((BigDecimal) right.getValue());
		}
	},
	MUL("*") {
		protected BigDecimal calculate(IValue left, IValue right) {
			return ((BigDecimal)left.getValue()).multiply((BigDecimal) right.getValue());
		}
	},
	DIV("/") {
		protected BigDecimal calculate(IValue left, IValue right) {
			return ((BigDecimal)left.getValue()).divide((BigDecimal) right.getValue());
		}
	},
	MOD("%") {
		protected BigDecimal calculate(IValue left, IValue right) {
			return ((BigDecimal)left.getValue()).remainder((BigDecimal) right.getValue());
		}
	};
	
	private String symbol;
	
	private BiFunction<BigDecimal, BigDecimal, BigDecimal> f; // TODO
	
	private ArithmeticOperator(String symbol) {
		this.symbol = symbol;
	}
	
	private static IDataType getDataType(IDataType left, IDataType right) {
		if(left.equals(IDataType.INT) && right.equals(IDataType.INT))
			return IDataType.INT;
		else if(left.equals(IDataType.DOUBLE) && right.equals(IDataType.INT))
			return IDataType.DOUBLE;
		else if(left.equals(IDataType.INT) && right.equals(IDataType.DOUBLE))
			return IDataType.DOUBLE;
		else if(left.equals(IDataType.DOUBLE) && right.equals(IDataType.DOUBLE))
			return IDataType.DOUBLE;
		else
			return IDataType.UNKNOWN;
	}
	
	
	@Override
	public String toString() {
		return symbol;
	}
	
	@Override
	public String getSymbol() {
		return symbol;
	}

	public IValue apply(IExpression left, IExpression right, IStackFrame frame) throws ExecutionError {
		IValue leftValue =  frame.evaluate(left);
		IValue rightValue = frame.evaluate(right);
		
		IDataType type = getDataType(left.getType(), right.getType());
		BigDecimal obj = calculate(leftValue, rightValue);
		return Value.create(type, obj);
	}
	
	protected abstract BigDecimal calculate(IValue left, IValue right);
	
	public IDataType getResultType(IExpression left, IExpression right) {
		return getDataType(left.getType(), right.getType());
	}
}
