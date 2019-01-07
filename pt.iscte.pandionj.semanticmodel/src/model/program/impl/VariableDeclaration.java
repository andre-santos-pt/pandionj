package model.program.impl;

import model.program.IDataType;
import model.program.IProcedure;
import model.program.IVariableDeclaration;

public class VariableDeclaration extends SourceElement implements IVariableDeclaration {

	private final IProcedure parent;
	private final String name;
	private final IDataType type;
	
	public VariableDeclaration(IProcedure parent, String name, IDataType type) {
		this.parent = parent;
		this.name = name;
		this.type = type;
	}

	
	@Override
	public String getIdentifier() {
		return name;
	}

	@Override
	public IProcedure getProcedure() {
		return parent;
	}

	@Override
	public IDataType getType() {
		return type;
	}

	@Override
	public int getArrayDimensions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isReference() {
		return false;
	}
	
	@Override
	public String toString() {
		return "var " + name + " (" + type + ")";
	}
}
