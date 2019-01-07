package model.program;

public enum Operator {
	ADD, MINUS, PROD, DIV, MOD,
	EQUAL, DIFFERENT;
	
	public boolean isArithmetic() {
		return this == ADD || this == MINUS || this == PROD || this == DIV || this == MOD;
	}
	
//	public abstract IExpression apply(IExpression left, IExpression right);
}
