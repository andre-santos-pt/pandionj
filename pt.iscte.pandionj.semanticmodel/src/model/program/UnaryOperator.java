package model.program;

import model.machine.IValue;
import model.machine.impl.Value;

enum UnaryOperator implements IUnaryOperator {
	TRUNCATE("(int)") {
		@Override
		protected Object calculate(IValue value) {
			assert value.getType().isNumeric();
			return (int) value.getValue();
		}
		
		@Override
		public IDataType getResultType(IExpression exp) {
			return IDataType.INT;
		}
	},
	NOT("!") {
		@Override
		protected Object calculate(IValue value) {
			assert value.getType() == IDataType.BOOLEAN;
			return ! (boolean) value.getValue();
		}
		
		@Override
		public IDataType getResultType(IExpression exp) {
			return IDataType.BOOLEAN;
		}
	}
	;
	
	private String symbol;
	
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
		IDataType type = IDataType.INT; // FIXME
		Object obj = calculate(value);
		return new Value(type, obj);
	}
	
	protected abstract Object calculate(IValue value);
	
	public abstract IDataType getResultType(IExpression exp);
}
