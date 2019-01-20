package impl.program;

import java.util.List;

import model.program.IArrayAllocation;
import model.program.IArrayElementExpression;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IBinaryExpression;
import model.program.IBinaryOperator;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IProgram;
import model.program.IUnaryExpression;
import model.program.IUnaryOperator;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;

public class Factory implements IFactory {
	@Override
	public IProgram createProgram() {
		return new Program();
	}

	@Override
	public IVariableExpression variableExpression(IVariableDeclaration variable) {
		return new VariableExpression(variable);
	}

	@Override
	public IArrayElementExpression arrayElementExpression(IArrayVariableDeclaration variable, List<IExpression> indexes) {
		return new ArrayElementExpression(variable, indexes);
	}

	@Override
	public IUnaryExpression unaryExpression(IUnaryOperator operator, IExpression exp) {
		return new UnaryExpression(operator, exp);
	}

	@Override
	public IBinaryExpression binaryExpression(IBinaryOperator operator, IExpression left, IExpression right) {
		return new BinaryExpression(operator, left, right);
	}

	@Override
	public ILiteral literal(IDataType type, String value) {
		return new Literal(type, value);
	}

	@Override
	public ILiteral literalMatch(String string) {
		for(IDataType t : IDataType.DEFAULTS)
			if(t.matchesLiteral(string))
				return literal(t, string);
		return null;
	}
	
	@Override
	public ILiteral literal(int value) {
		return new Literal(IDataType.INT, Integer.toString(value));
	}

	@Override
	public ILiteral literal(double value) {
		return new Literal(IDataType.DOUBLE, Double.toString(value));
	}

	@Override
	public ILiteral literal(boolean value) {
		return new Literal(IDataType.BOOLEAN, Boolean.toString(value));
	}

	@Override
	public IArrayAllocation arrayAllocation(IDataType type, List<IExpression> dimensions) {
		return new ArrayAllocation(type, dimensions);
	}

	@Override
	public IArrayType arrayType(IDataType componentType, int dimensions) {
		return new ValueTypeArray(componentType, dimensions);
	}	
}
