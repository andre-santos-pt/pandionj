package model.program.impl;

import java.util.List;

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

public class Loop extends Statement implements ILoop {
	private final IExpression guard;
	private final IBlock block;
	
	public Loop(Block parent, IExpression guard) {
		super(parent);
		this.guard = guard;
		this.block = new Block(parent);
	}
	
	@Override
	public IExpression getGuard() {
		return guard;
	}

	@Override
	public IBlock getBlock() {
		return block;
	}

	@Override
	public boolean executeBlockFirst() {
		return false;
	}

	@Override
	public String toString() {
		return "while(" + guard + ") " + block;
	}
	
	@Override
	public IBreak breakStatement() {
		return new Break(this);
	}
	
	@Override
	public IContinue continueStatement() {
		return new Continue(this);
	}
	
	private static class Break extends Statement implements IBreak {
		public Break(ILoop parent) {
			super(parent);
		}
		
		@Override
		public String toString() {
			return "break";
		}
	}
	
	private static class Continue extends Statement implements IContinue {
		public Continue(ILoop parent) {
			super(parent);
		}
		
		@Override
		public String toString() {
			return "continue";
		}
	}

	@Override
	public List<IStatement> getStatements() {
		return block.getStatements();
	}
	
	public void addStatement(IStatement statement) {
		block.addStatement(statement);
	}

	@Override
	public IVariableDeclaration variableDeclaration(String name, IDataType type) {
		return block.variableDeclaration(name, type);
	}

	@Override
	public IArrayVariableDeclaration arrayDeclaration(String name, IDataType type, int dimensions) {
		return block.arrayDeclaration(name, type, dimensions);
	}

	@Override
	public IVariableAssignment assignment(IVariableDeclaration var, IExpression exp) {
		return block.assignment(var, exp);
	}

	@Override
	public ISelection selection(IExpression expression, IBlock block, IBlock alternativeBlock) {
		return block.selection(expression, block, alternativeBlock);
	}

	@Override
	public ILoop loop(IExpression guard) {
		return block.loop(guard);
	}

	@Override
	public IReturn returnStatement(IExpression expression) {
		return block.returnStatement(expression);
	}

	@Override
	public IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args) {
		return block.procedureCall(procedure, args);
	}

	@Override
	public IBlock block() {
		return block.block();
	}
}
