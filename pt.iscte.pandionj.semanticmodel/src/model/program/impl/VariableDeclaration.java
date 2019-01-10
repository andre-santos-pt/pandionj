package model.program.impl;

import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;

class VariableDeclaration extends Statement implements IVariableDeclaration {

	private final String name;
	private final IDataType type;
	private final boolean constant; // TODO final vars
	
	public VariableDeclaration(Block block, String name, IDataType type, boolean constant) {
		super(block);
		this.name = name;
		this.type = type;
		this.constant = constant;
	}

	
	@Override
	public String getIdentifier() {
		return name;
	}
	
	@Override
	public boolean isConstant() {
		return constant;
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
		return false;
	}
	
	@Override
	public String toString() {
		return "var " + name + " (" + type + ")";
	}
	
	@Override
	public IVariableAssignment assignment(IExpression expression) {
		return new VariableAssignment(getParent(), this, expression);
	}
	
	@Override
	public IVariableExpression expression() {
		return new VariableExpression(this);
	}
}
