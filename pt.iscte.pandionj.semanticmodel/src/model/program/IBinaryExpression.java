package model.program;

public interface IBinaryExpression extends IExpression {
	IBinaryOperator getOperator();
	IExpression getLeftExpression();
	IExpression getRightExpression();
	
	boolean isBoolean();
}
