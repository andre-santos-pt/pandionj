package model.program.impl;

import com.google.common.collect.ImmutableList;

import model.program.IBlock;
import model.program.IProcedure;
import model.program.IVariableDeclaration;

public class Procedure implements IProcedure {
	private final String name;
	private final ImmutableList<IVariableDeclaration> params;
	private final IBlock body;
	
	public Procedure(String name, ImmutableList<IVariableDeclaration> params, IBlock body) {
		this.name = name;
		this.params = params;
		this.body = body;
	}

	@Override
	public String getIdentifier() {
		return name;
	}

	@Override
	public ImmutableList<IVariableDeclaration> getParameters() {
		return params;
	}

	@Override
	public IBlock getBody() {
		return body;
	}

	@Override
	public ImmutableList<IVariableDeclaration> getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFunction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRecursive() {
		// TODO Auto-generated method stub
		return false;
	}

}
