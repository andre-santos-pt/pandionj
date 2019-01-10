package model.program;

import model.machine.IValue;

enum LogicalOperator implements IBinaryOperator {
	AND("&&") {
		@Override
		protected boolean calculate(IValue left, IValue right) {
			return (boolean) left.getValue() && (boolean) right.getValue(); // FIXME;
		}
	}
//	OR("||"), XOR("^"),
	;

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

	public IValue apply(IValue left, IValue right) {
		return calculate(left, right) ? IValue.TRUE : IValue.FALSE;
	}

	protected abstract boolean calculate(IValue left, IValue right);
	
	public IDataType getResultType(IExpression left, IExpression right) {
		return IDataType.BOOLEAN;
	}
}
