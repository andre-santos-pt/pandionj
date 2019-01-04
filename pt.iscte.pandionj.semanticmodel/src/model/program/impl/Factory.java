package model.program.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IBinaryExpression;
import model.program.IBlock;
import model.program.IConstantDeclaration;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.ISelection;
import model.program.IStatement;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;
import model.program.Operator;

public class Factory implements IFactory {
	@Override
	public IProcedure createProcedure(String name, ImmutableList<IVariableDeclaration> parameters) {
		return new Procedure(name, parameters, createBlock());
	}

	@Override
	public IProcedure createProcedure(String name, ImmutableList<IVariableDeclaration> parameters,
			IStatement... statements) {
		return new Procedure(name, parameters, createBlock(statements));
	}

	@Override
	public IBlock createBlock(IStatement ... statements) {
		return new Block(ImmutableList.copyOf(statements));
	}
	
	@Override
	public IBlock createBlock(List<IStatement> statements) {
		return new Block(ImmutableList.copyOf(statements));
	}

	@Override
	public IProgram createProgram(ImmutableList<IProcedure> procedures, IProcedure main,
			ImmutableList<IConstantDeclaration> constants) {
		return new Program(procedures, main);
	}
	
	@Override
	public IVariableDeclaration createVariableDeclaration(IProcedure parent, String name, IDataType type) {
		return new VariableDeclaration(parent, name, type);
	}

	@Override
	public IVariableExpression createVariableExpression(IVariableDeclaration variable) {
		return new VariableExpression(variable);
	}

	@Override
	public ILiteral createLiteral(String value) {
		return new Literal(value);
	}

	@Override
	public IVariableAssignment createAssignment(IVariableDeclaration var, IExpression exp) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ISelection createSelection(IExpression expression, IBlock block) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISelection createSelection(IExpression expression, IStatement statement) {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public IBinaryExpression createBinaryExpression(Operator operator, IExpression left, IExpression right) {
		// TODO Auto-generated method stub
		return null;
	}

}
