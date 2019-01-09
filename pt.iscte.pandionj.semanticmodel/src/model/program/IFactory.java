package model.program;

import java.util.Arrays;
import java.util.List;

public interface IFactory {

	IProgram createProgram();
	
//	IProcedure createProcedure(String name, IDataType returnType);
	
	
	
	// EXPRESSIONS
	
	IVariableExpression createVariableExpression(IVariableDeclaration var);
	
	IBinaryExpression createBinaryExpression(Operator operator, IExpression left, IExpression right);

	ILiteral createLiteral(IDataType type, String string);
	ILiteral value(int value);
	ILiteral value(double value);
	ILiteral value(boolean value);
	
	IProcedureCall createProcedureCall(IProcedure procedure, List<IExpression> args);
	default IProcedureCall createProcedureCall(IProcedure procedure, IExpression ... args) {
		return createProcedureCall(procedure, Arrays.asList(args));
	}
}
