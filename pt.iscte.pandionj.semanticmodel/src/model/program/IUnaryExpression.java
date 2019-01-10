package model.program;

public interface IUnaryExpression extends IExpression {
	IBinaryOperator getOperator();
	IExpression getExpression();
}
