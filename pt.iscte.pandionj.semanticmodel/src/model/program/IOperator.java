package model.program;

public interface IOperator {
	String getSymbol();
	
	
	IBinaryOperator ADD = ArithmeticOperator.ADD;
	IBinaryOperator SUB = ArithmeticOperator.SUB;
	IBinaryOperator MUL = ArithmeticOperator.MUL;
	IBinaryOperator DIV = ArithmeticOperator.DIV;
	IBinaryOperator MOD = ArithmeticOperator.MOD;
	
	IBinaryOperator EQUAL = RelationalOperator.EQUAL;
	IBinaryOperator DIFFERENT = RelationalOperator.DIFFERENT;
	IBinaryOperator GREATER = RelationalOperator.GREATER;
	IBinaryOperator GREATER_EQ = RelationalOperator.GREATER_EQ;
	IBinaryOperator SMALLER = RelationalOperator.SMALLER;
	IBinaryOperator SMALLER_EQ = RelationalOperator.SMALLER_EQ;
	
	IUnaryOperator NOT = UnaryOperator.NOT;
	
	IBinaryOperator AND = LogicalOperator.AND;
	IBinaryOperator OR = LogicalOperator.OR;
	IBinaryOperator XOR = LogicalOperator.XOR;
	
//	IUnaryOperator TRUNCATE = UnaryOperator.TRUNCATE;

	
}
