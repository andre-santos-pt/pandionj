package model.program.impl;

import java.util.List;

import model.machine.IValue;
import model.machine.impl.Value;
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
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProgram;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;

public class Factory implements IFactory {
	//	@Override
	//	public IProcedure createProcedure(String name, IDataType returnType) {
	//		return new Procedure(name, returnType);
	//	}

	//	@Override
	//	public IProcedure createProcedure(String name, ImmutableList<IVariableDeclaration> parameters,
	//			IStatement... statements) {
	//		IProcedure p = new Procedure(name, parameters);
	//		p.setBody(createBlock(statements));
	//		return p;
	//	}

	//	@Override
	//	public IBlock createBlock(IBlock parent, IStatement ... statements) {
	//		return new Block(parent);
	//	}

	//	@Override
	//	public IBlock createBlock(List<IStatement> statements) {
	//		return new Block(ImmutableList.copyOf(statements));
	//	}

	@Override
	public IProgram createProgram() {
		return new Program();
	}

	//	@Override
	//	public IVariableDeclaration createVariableDeclaration(IProcedure parent, String name, IDataType type) {
	//		return new VariableDeclaration(parent, name, type, false);
	//	}
	//
	@Override
	public IVariableExpression variableExpression(IVariableDeclaration variable) {
		return new VariableExpression(variable);
	}
	
	@Override
	public IArrayElementExpression arrayElementExpression(IArrayVariableDeclaration variable, List<IExpression> indexes) {
		return new ArrayElementExpression(variable, indexes);
	}

	@Override
	public IBinaryExpression unaryExpression(IBinaryOperator operator, IExpression exp) {
		// TODO Auto-generated method stub
		return null;
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

	public IValue getArray(IArrayType type, Object ... elements) {
		Object[] array = new Object[elements.length];
		for(int i = 0; i < elements.length; i++)
			array[i] = elements[i];
		return new Value(type, array);
	}
	
	@Override
	public IArrayAllocation arrayAllocation(IDataType type, List<IExpression> dimensions) {
		return new ArrayAllocation(type, dimensions);
	}
	
	//	@Override
	//	public IVariableAssignment createAssignment(IVariableDeclaration variable, IExpression expression) {
	//		return new VariableAssignment(variable, expression);
	//	}
	//	
	//	@Override
	//	public IReturn createReturn(IExpression expression) {
	//		return new Return(expression);
	//	}
	//	
	@Override
	public IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args) {
		return new ProcedureCall(null, procedure, args);
	}
	//	
	//	
	//
	//	@Override
	//	public ISelection createSelection(IExpression guard, IBlock block) {
	//		return createSelection(guard, block, null);
	//	}
	//
	//	@Override
	//	public ISelection createSelection(IExpression guard, IBlock block, IBlock alternativeBlock) {
	//		return new Selection(guard, block, alternativeBlock);
	//	}






}
