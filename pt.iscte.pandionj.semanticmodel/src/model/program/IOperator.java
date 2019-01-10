package model.program;

public interface IOperator {
	String getSymbol();
	
	
	IBinaryOperator ADD = ArithmeticOperator.ADD;
	IBinaryOperator GREATER = RelationalOperator.GREATER;
	
	IUnaryOperator NOT = UnaryOperator.NOT;
	IBinaryOperator AND = LogicalOperator.AND;
	
	IUnaryOperator TRUNCATE = UnaryOperator.TRUNCATE;
	
}
