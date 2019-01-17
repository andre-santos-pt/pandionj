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
import model.program.IStructMemberAssignment;
import model.program.IStructType;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;

class Block extends Statement implements IBlock {
	private final IBlock parent;
	private final List<IStatement> statements;

	Block(IBlock parent, boolean addToParent) {
		super(parent, addToParent);
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
	
	void addStatement(IStatement statement) {
		assert statement != null;
		statements.add(statement);
	}

	@Override
	public IBlock addBlock() {
		return new Block(this, true);
	}

	IBlock addLooseBlock() {
		return new Block(this, false);
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
	public IVariableDeclaration addVariableDeclaration(String name, IDataType type, Set<IVariableDeclaration.Flag> flags) {
		return new VariableDeclaration(this, name, type, flags);
	}
	
	@Override
	public IArrayVariableDeclaration addArrayDeclaration(String name, IArrayType type, Set<IVariableDeclaration.Flag> flags) {
		return new ArrayVariableDeclaration(this, name, type, flags);
	}

	@Override
	public IVariableAssignment addAssignment(IVariableDeclaration variable, IExpression expression) {
		return new VariableAssignment(this, variable, expression);
	}

	@Override
	public IArrayElementAssignment addArrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, List<IExpression> indexes) {
		return new ArrayElementAssignment(this, var, indexes, exp);
	}
	
	@Override
	public IStructMemberAssignment addStructMemberAssignment(IVariableDeclaration var, String memberId, IExpression exp) {
		return new StructMemberAssignment(this, var, memberId, exp);
	}
	
	@Override
	public ISelection addSelection(IExpression guard) {
		return new Selection(this, guard);
	}

	@Override
	public ILoop addLoop(IExpression guard) {
		return new Loop(this, guard, false);
	}

	@Override
	public IReturn addReturnStatement(IExpression expression) {
		return new Return(this, expression);
	}
	
	@Override
	public IProcedureCall addProcedureCall(IProcedure procedure, List<IExpression> args) {
		return new ProcedureCall(this, procedure, args);
	}
}
