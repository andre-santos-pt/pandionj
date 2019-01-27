package model.program;

import java.math.BigDecimal;
import java.util.function.BiFunction;

import impl.machine.ExecutionError;
import impl.machine.Value;
import model.machine.IValue;

public enum RelationalOperator implements IBinaryOperator {
	EQUAL("==", (left,right) -> left.getValue().equals(right.getValue())),
	
	DIFFERENT("!=", (left,right) -> !left.getValue().equals(right.getValue())),
	
	GREATER(">", (left, right) -> compare(left, right) > 0),
	
	GREATER_EQUAL(">=", (left, right) -> compare(left, right) >= 0),
	
	SMALLER("<", (left, right) -> compare(left, right) < 0),
	
	SMALLER_EQUAL("<=", (left, right) -> compare(left, right) <= 0);

	private static int compare(IValue left, IValue right) {
		return ((BigDecimal) left.getValue()).compareTo((BigDecimal) right.getValue());
	}
	
	private final String symbol;
	private final BiFunction<IValue, IValue, Boolean> f;
	
	private RelationalOperator(String symbol, BiFunction<IValue, IValue, Boolean> f) {
		this.symbol = symbol;
		this.f = f;
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
		return Value.create(IDataType.BOOLEAN, f.apply(left, right));
	}
	
	@Override
	public IDataType getResultType(IExpression left, IExpression right) {
		return IDataType.BOOLEAN;
	}
	
	@Override
	public OperationType getOperationType() {
		return OperationType.RELATIONAL;
	}
}
