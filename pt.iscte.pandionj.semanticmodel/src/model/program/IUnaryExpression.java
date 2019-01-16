package model.program;

public interface IUnaryExpression extends IExpression {
	IUnaryOperator getOperator();
	IExpression getExpression();
}
