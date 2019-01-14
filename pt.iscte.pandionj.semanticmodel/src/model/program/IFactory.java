package model.program;

import java.util.Arrays;
import java.util.List;

public interface IFactory {

	IProgram createProgram();
	
	// EXPRESSIONS
	
	IVariableExpression variableExpression(IVariableDeclaration var);
	
	IArrayElementExpression arrayElementExpression(IArrayVariableDeclaration var, List<IExpression> indexes);
	
	IUnaryExpression unaryExpression(IUnaryOperator operator, IExpression exp);
	IBinaryExpression binaryExpression(IBinaryOperator operator, IExpression left, IExpression right);

	ILiteral literal(IDataType type, String string);
	ILiteral literal(int value);
	ILiteral literal(double value);
	ILiteral literal(boolean value);
	
//	IValue getArray(IArrayType type, Object ... elements);
	
	IArrayAllocation arrayAllocation(IDataType type, List<IExpression> dimensions);
	default IArrayAllocation arrayAllocation(IDataType type, IExpression ... dimensions) {
		return arrayAllocation(type, Arrays.asList(dimensions));
	}
	
	IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args);
	default IProcedureCall procedureCall(IProcedure procedure, IExpression ... args) {
		return procedureCall(procedure, Arrays.asList(args));
	}
	
	
	
}
