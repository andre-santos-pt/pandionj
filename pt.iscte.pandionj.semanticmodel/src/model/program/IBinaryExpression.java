package model.program;

public interface IBinaryExpression extends IExpression {
	Operator getOperator();
	IExpression getLeftExpression();
	IExpression getRightExpression();
}
