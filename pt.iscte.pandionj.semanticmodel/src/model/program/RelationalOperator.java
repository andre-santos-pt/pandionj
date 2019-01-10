package model.program;

import model.machine.IValue;

enum RelationalOperator implements IBinaryOperator {
	EQUAL("==") {
		@Override
		protected boolean calculate(IValue left, IValue right) {
			return left.getValue().equals(right.getValue()); // FIXME;
		}
	},
	DIFFERENT("!=") {
		@Override
		protected boolean calculate(IValue left, IValue right) {
			return !EQUAL.calculate(left, right); // FIXME;
		}
	},
	GREATER(">") {
		@Override
		protected boolean calculate(IValue left, IValue right) {
			return ((Number) left.getValue()).doubleValue() > ((Number) right.getValue()).doubleValue(); // FIXME;
		}
	}
//	GREATER_EQ(">="), SMALLER("<"), SMALLER_EQ("<="),
	;

	private String symbol;

	private RelationalOperator(String symbol) {
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
