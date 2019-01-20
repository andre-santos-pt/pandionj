package model.program;

import java.util.Arrays;
import java.util.List;

import impl.program.Factory;

public interface IFactory {
	IFactory INSTANCE = new Factory();
	
	IProgram createProgram();
	
	IVariableExpression variableExpression(IVariableDeclaration var);
	
	IArrayElementExpression arrayElementExpression(IArrayVariableDeclaration var, List<IExpression> indexes);
	
	IUnaryExpression unaryExpression(IUnaryOperator operator, IExpression exp);
	
	IBinaryExpression binaryExpression(IBinaryOperator operator, IExpression left, IExpression right);

	ILiteral literal(IDataType type, String string);
	ILiteral literalMatch(String string);
	ILiteral literal(int value);
	ILiteral literal(double value);
	ILiteral literal(boolean value);
	
	IArrayAllocation arrayAllocation(IDataType type, List<IExpression> dimensions);
	default IArrayAllocation arrayAllocation(IDataType type, IExpression ... dimensions) {
		return arrayAllocation(type, Arrays.asList(dimensions));
	}
	
	IArrayType arrayType(IDataType componentType, int dimensions);
}
