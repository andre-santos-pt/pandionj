package model.program.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IBinaryExpression;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProgram;
import model.program.IReturn;
import model.program.ISelection;
import model.program.IStatement;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;
import model.program.Operator;

public class Factory implements IFactory {
	@Override
	public IProcedure createProcedure(String name, ImmutableList<IVariableDeclaration> parameters) {
		return new Procedure(name, parameters);
	}

//	@Override
//	public IProcedure createProcedure(String name, ImmutableList<IVariableDeclaration> parameters,
//			IStatement... statements) {
//		IProcedure p = new Procedure(name, parameters);
//		p.setBody(createBlock(statements));
//		return p;
//	}

	@Override
	public IBlock createBlock(IBlock parent, IStatement ... statements) {
		return new Block(parent, ImmutableList.copyOf(statements));
	}

//	@Override
//	public IBlock createBlock(List<IStatement> statements) {
//		return new Block(ImmutableList.copyOf(statements));
//	}

	@Override
	public IProgram createProgram() {
		return new Program();
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
	public IVariableAssignment createAssignment(IVariableDeclaration variable, IExpression expression) {
		return new VariableAssignment(variable, expression);
	}
	
	@Override
	public IReturn createReturn(IExpression expression) {
		return new Return(expression);
	}
	
	@Override
	public IProcedureCall createProcedureCall(IProcedure procedure, List<IExpression> args) {
		return new ProcedureCall(procedure, args);
	}
	
	@Override
	public IBinaryExpression createBinaryExpression(Operator operator, IExpression left, IExpression right) {
		return new BinaryExpression(operator, left, right);
	}
	

	@Override
	public ISelection createSelection(IExpression guard, IBlock block) {
		return createSelection(guard, block, null);
	}

	@Override
	public ISelection createSelection(IExpression guard, IBlock block, IBlock alternativeBlock) {
		return new Selection(guard, block, alternativeBlock);
	}


	

	

}
