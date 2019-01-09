package model.program;

public enum Operator {
	ADD("+"), MINUS("-"), PROD("*"), DIV("/"), MOD("%"),
	EQUAL("=="), DIFFERENT("!="), GREATER(">"), GREATER_EQ(">="), SMALLER("<"), SMALLER_EQ("<="),
	AND("&&"), OR("||"), NOT("!"),
	CAST_INT("(int)"), CAST_DOUBLE("(double)");
	
	private String symbol;
	
	private Operator(String symbol) {
		
	}
	
	public boolean isNumeric() {
		return this == ADD || this == MINUS || this == PROD || this == DIV || this == MOD;
	}

	public boolean isBoolean() {
		return this == EQUAL || this == DIFFERENT || 
				this == GREATER || this == GREATER_EQ || this == SMALLER || this == SMALLER_EQ;
	}
	
	public boolean isLogical() {
		return this == AND || this == OR || this == NOT;
	}
	
	public boolean isUnary() {
		return this == NOT;
	}
	public boolean isBinary() {
		return !isUnary();
	}
	
	@Override
	public String toString() {
		return symbol;
	}
	
//	public abstract IExpression apply(IExpression left, IExpression right);
}
