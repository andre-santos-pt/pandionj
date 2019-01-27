package impl.program;

import model.program.IDataType;
import model.program.IExpression;
import model.program.IProgramElement;
import model.program.IStructMemberAssignment;
import model.program.IStructMemberExpression;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;

class UndeclaredVariable extends ProgramElement implements IVariableDeclaration {
	private final String id;

	public UndeclaredVariable(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public IProgramElement getParent() {
		return null;
	}

	@Override
	public IDataType getType() {
		return IDataType.UNKNOWN;
	}

	@Override
	public boolean isReference() {
		return false;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isParameter() {
		return false;
	}

	@Override
	public IVariableExpression expression() {
		return new VariableExpression(this);
	}

	@Override
	public IStructMemberExpression memberExpression(String memberId) {
		return null;
	}

	@Override
	public IVariableAssignment addAssignment(IExpression exp) {
		return null;
	}

	@Override
	public IStructMemberAssignment addMemberAssignment(String memberId, IExpression expression) {
		return null;
	}
}