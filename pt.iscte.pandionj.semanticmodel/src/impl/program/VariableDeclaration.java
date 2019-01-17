package impl.program;

import java.util.Set;

import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IIdentifiableElement;
import model.program.IProcedure;
import model.program.ISourceElement;
import model.program.IStructMemberAssignment;
import model.program.IStructMemberExpression;
import model.program.IStructType;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;
import model.program.roles.IGatherer;
import model.program.roles.IVariableRole;

class VariableDeclaration extends SourceElement implements IVariableDeclaration {

	private final ISourceElement parent;
	private final String name;
	private final IDataType type;
	private final boolean constant; // TODO final vars
	private final boolean reference;
	private final boolean param;
	private final boolean field;
	private IVariableRole role;

	public VariableDeclaration(ISourceElement parent, String name, IDataType type, Set<Flag> flags) {
//		super(block, true);
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
	public ISourceElement getParent() {
		return parent;
	}
	
//	@Override
//	public IProcedure getProcedure() {
//		IBlock parent = getParent();
//		while(!(parent instanceof IProcedure))
//			parent = parent.getParent();
//		return (IProcedure) parent;
//	}

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
