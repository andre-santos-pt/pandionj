package impl.program;

import java.util.Set;

import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IIdentifiableElement;
import model.program.IProcedure;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;
import model.program.roles.IGatherer;
import model.program.roles.IVariableRole;

class VariableDeclaration extends Statement implements IVariableDeclaration {

	private final String name;
	private final IDataType type;
	private final boolean constant; // TODO final vars
	private final boolean reference;
	private final boolean param;
	private final boolean field;
	private IVariableRole role;

	public VariableDeclaration(Block block, String name, IDataType type, Set<Flag> flags) {
		super(block);
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
	public IProcedure getProcedure() {
		IBlock parent = getParent();
		while(!(parent instanceof IProcedure))
			parent = parent.getParent();
		return (IProcedure) parent;
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
		return (isReference() ? "*var " : "var ") + name + " (" + type.getId() + ", " + getRole() + ")";
	}

	@Override
	public IVariableAssignment assignment(IExpression expression) {
		return new VariableAssignment(getParent(), this, expression);
	}

	@Override
	public IVariableExpression expression() {
		return new VariableExpression(this);
	}

	@Override
	public IVariableRole getRole() {
		if(field)
			role = IVariableRole.NONE;
		
		if(role == null) {
			if(IGatherer.isGatherer(this))
				role = IGatherer.createGatherer(this);
			else
				role = IVariableRole.NONE;
		}
		return role;
	}
}
