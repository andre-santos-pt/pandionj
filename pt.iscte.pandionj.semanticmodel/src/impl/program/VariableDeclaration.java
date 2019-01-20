package impl.program;

import java.util.Set;

import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IIdentifiableElement;
import model.program.IProgramElement;
import model.program.IStructMemberAssignment;
import model.program.IStructMemberExpression;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;

class VariableDeclaration extends ProgramElement implements IVariableDeclaration {

	private final IProgramElement parent;
	private final String name;
	private final IDataType type;
	private final boolean constant; // TODO final vars
	private final boolean reference;
	private final boolean param;
	private final boolean field;

	public VariableDeclaration(IProgramElement parent, String name, IDataType type, Set<Flag> flags) {
		this.parent = parent;
		assert IIdentifiableElement.isValidIdentifier(name);
		this.name = name;
		this.type = type;
		this.constant = flags.contains(Flag.CONSTANT);
		this.reference = flags.contains(Flag.REFERENCE);
		this.param = flags.contains(Flag.PARAM);
		this.field = flags.contains(Flag.FIELD);
	}


	@Override
	public String getId() {
		return name;
	}

	@Override
	public boolean isConstant() {
		return constant;
	}

	public boolean isParameter() {
		return param;
	}

	@Override
	public IProgramElement getParent() {
		return parent;
	}
	
	@Override
	public IDataType getType() {
		return type;
	}

	@Override
	public boolean isReference() {
		return reference;
	}

	@Override
	public String toString() {
		return (isReference() ? "*var " : "var ") + name + " (" + type.getId() + ")";
	}

	@Override
	public IVariableAssignment addAssignment(IExpression expression) {
		assert parent instanceof IBlock;
		return new VariableAssignment((IBlock) parent, this, expression);
	}

	@Override
	public IVariableExpression expression() {
		return new VariableExpression(this);
	}
	
	@Override
	public IStructMemberAssignment addMemberAssignment(String memberId, IExpression expression) {
		assert parent instanceof IBlock;
		return new StructMemberAssignment((IBlock) parent, this, memberId, expression);
	}
	
	@Override
	public IStructMemberExpression memberExpression(String memberId) {
		return new StructMemberExpression(this, memberId);
	}
}
