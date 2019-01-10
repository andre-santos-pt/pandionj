package model.program;

import model.machine.IValue;
import model.machine.impl.Value;

enum ArithmeticOperator implements IBinaryOperator {
	ADD("+") {
		@Override
		protected Object calculateBinary(IValue left, IValue right) {
			return (int) left.getValue() + (int) right.getValue(); // FIXME
		}
	}, 
//	MINUS("-"), 
//	PROD("*"), 
//	DIV("/"), 
//	MOD("%"),
	;
	
	private String symbol;
	
	private ArithmeticOperator(String symbol) {
		this.symbol = symbol;
	}
	
	private static IDataType getNumericType(IDataType left, IDataType right) {
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

	public IValue apply(IValue left, IValue right) {
		IDataType type = getNumericType(left.getType(), right.getType());
		Object obj = calculateBinary(left, right);
		return new Value(type, obj);
	}
	
	protected abstract Object calculateBinary(IValue left, IValue right);
	
	public IDataType getResultType(IExpression left, IExpression right) {
		return getNumericType(left.getType(), right.getType());
	}
}
