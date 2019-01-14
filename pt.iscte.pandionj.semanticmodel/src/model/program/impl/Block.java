package model.program.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import model.program.IArrayVariableDeclaration;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.ILoop;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IReturn;
import model.program.ISelection;
import model.program.IStatement;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;

class Block extends SourceElement implements IBlock{

	private final IBlock parent;
	private final List<IStatement> statements;

	public Block(IBlock parent) {
		this.parent = parent;
		this.statements = new ArrayList<>();
	}
	
//	public Block(IBlock parent, IStatement ... statements) {
//		this(parent);
//		for(IStatement s : statements)
//			this.statements.add(s);
//	}

	@Override
	public IBlock getParent() {
		return parent;
	}
	
	@Override
	public List<IStatement> getStatements() {
		return Collections.unmodifiableList(statements);
	}
	
	public void addStatement(IStatement statement) {
		assert statement != null;
		statements.add(statement);
	}

	@Override
	public IBlock block() {
		return new Block(this);
	}

	@Override
	public IVariableDeclaration variableDeclaration(String name, IDataType type, Set<IVariableDeclaration.Flag> flags) {
		return new VariableDeclaration(this, name, type, flags);
	}
	
	@Override
	public IArrayVariableDeclaration arrayDeclaration(String name, IDataType type, int dimensions, Set<IVariableDeclaration.Flag> flags) {
		return new ArrayVariableDeclaration(this, name, type, dimensions, flags);
	}

	@Override
	public IVariableAssignment assignment(IVariableDeclaration variable, IExpression expression) {
		return new VariableAssignment(this, variable, expression);
	}

	@Override
	public ISelection selection(IExpression guard, IBlock block, IBlock alternativeBlock) {
		return new Selection(this, guard, block, alternativeBlock);
	}

	@Override
	public ILoop loop(IExpression guard) {
		return new Loop(this, guard);
	}

	@Override
	public IReturn returnStatement(IExpression expression) {
		return new Return(this, expression);
	}
	
	@Override
	public IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args) {
		return new ProcedureCall(this, procedure, args);
	}

	@Override
	public String toString() {
		String text = "{ ";
		for(IStatement s : statements)
			text += s + "; ";
		return text + "}";
	}
}
