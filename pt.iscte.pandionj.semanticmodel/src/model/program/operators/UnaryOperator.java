package model.program.operators;

import java.math.BigDecimal;

import impl.machine.Value;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IUnaryOperator;

public enum UnaryOperator implements IUnaryOperator {
	NOT("!") {
		@Override
		protected Object calculate(IValue value) {
			assert value.getType() == IDataType.BOOLEAN;
			return !(boolean) value.getValue();
		}
		
		@Override
		public IDataType getResultType(IExpression exp) {
			return IDataType.BOOLEAN;
		}
		
		@Override
		public OperationType getOperationType() {
			return OperationType.LOGICAL;
		}
	},
	TRUNCATE("(int)") {
		@Override
		protected Object calculate(IValue value) {
			assert value.getType().isNumeric();
			return new BigDecimal(((BigDecimal) value.getValue()).intValue());
		}
		
		@Override
		public IDataType getResultType(IExpression exp) {
			return IDataType.INT;
		}
		
		@Override
		public OperationType getOperationType() {
			return OperationType.ARITHMETIC;
		}
	},
	
	// TODO NEGATION
	// TODO PLUS
	;
	
	private final String symbol;
	
	private UnaryOperator(String symbol) {
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

	@Override
	public IValue apply(IValue value) {
		Object obj = calculate(value);
		return Value.create(getResultType(null), obj);
	}
	
	protected abstract Object calculate(IValue value);
	
	public abstract IDataType getResultType(IExpression exp);
}
