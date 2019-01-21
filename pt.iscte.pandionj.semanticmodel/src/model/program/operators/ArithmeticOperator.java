package model.program.operators;

import java.math.BigDecimal;
import java.util.function.BiFunction;

import impl.machine.ExecutionError;
import impl.machine.Value;
import model.machine.IValue;
import model.program.IBinaryOperator;
import model.program.IDataType;
import model.program.IExpression;

public enum ArithmeticOperator implements IBinaryOperator {
	ADD("+", (left, right) -> left.add(right)), 
	SUB("-", (left, right) -> left.subtract(right)),
	MUL("*", (left, right) -> left.multiply(right)),
	DIV("/", (left, right) -> left.divide(right)),
	MOD("%", (left, right) -> left.remainder(right));
	
	private final String symbol;
	
	private final BiFunction<BigDecimal, BigDecimal, BigDecimal> f;
	
	private ArithmeticOperator(String symbol, BiFunction<BigDecimal, BigDecimal, BigDecimal> f) {
		this.symbol = symbol;
		this.f = f;
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

	@Override
	public IValue apply(IValue left, IValue right) throws ExecutionError {
		IDataType type = getDataType(left.getType(), right.getType());
		BigDecimal obj = f.apply((BigDecimal) left.getValue(), (BigDecimal) right.getValue());
		return Value.create(type, obj);
	}
	
//	protected abstract BigDecimal calculate(IValue left, IValue right);
	
	public IDataType getResultType(IExpression left, IExpression right) {
		return getDataType(left.getType(), right.getType());
	}
	
	@Override
	public OperationType getOperationType() {
		return OperationType.ARITHMETIC;
	}
}
