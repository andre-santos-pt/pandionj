package impl.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import model.program.IArrayElementAssignment;
import model.program.IArrayType;
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

class Block extends SourceElement implements IBlock {
	private final IBlock parent;
	private final List<IStatement> statements;

	public Block(IBlock parent) {
		this.parent = parent;
		this.statements = new ArrayList<>();
	}
	
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
	public String toString() {
		String tabs = "";
		int d = getDepth();
		for(int i = 0; i < d; i++)
			tabs += "\t";
		String text = "{";
		for(IStatement s : statements)
			text += tabs + s + ";";
		return tabs + text + "}";
	}
	
	@Override
	public IVariableDeclaration variableDeclaration(String name, IDataType type, Set<IVariableDeclaration.Flag> flags) {
		return new VariableDeclaration(this, name, type, flags);
	}
	
	@Override
	public IArrayVariableDeclaration arrayDeclaration(String name, IArrayType type, Set<IVariableDeclaration.Flag> flags) {
		return new ArrayVariableDeclaration(this, name, type, flags);
	}

	@Override
	public IVariableAssignment assignment(IVariableDeclaration variable, IExpression expression) {
		return new VariableAssignment(this, variable, expression);
	}

	@Override
	public IArrayElementAssignment arrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, List<IExpression> indexes) {
		return new ArrayElementAssignment(this, var, indexes, exp);
	}
	
	@Override
	public ISelection selection(IExpression guard, IBlock block, IBlock alternativeBlock) {
		return new Selection(this, guard, block, alternativeBlock);
	}

	@Override
	public ILoop loop(IExpression guard) {
		return new Loop(this, guard, false);
	}

	@Override
	public IReturn returnStatement(IExpression expression) {
		return new Return(this, expression);
	}
	
	@Override
	public IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args) {
		return new ProcedureCall(this, procedure, args);
	}
}
