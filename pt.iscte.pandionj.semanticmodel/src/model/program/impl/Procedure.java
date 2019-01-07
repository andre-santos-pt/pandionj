package model.program.impl;

import com.google.common.collect.ImmutableList;

import model.program.IBlock;
import model.program.IProcedure;
import model.program.IStatement;
import model.program.IVariableDeclaration;

public class Procedure extends SourceElement implements IProcedure {
	private final String name;
	private final ImmutableList<IVariableDeclaration> params;
	private IBlock body;
	
	public Procedure(String name, ImmutableList<IVariableDeclaration> params) {
		this.name = name;
		this.params = params;
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
	public void setBody(IBlock body) {
		this.body = body;
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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name + "()";
	}

	@Override
	public IBlock getParent() {
		return null;
	}

	@Override
	public ImmutableList<IStatement> getStatements() {
		return body == null ? ImmutableList.of() : body.getStatements();
	}

}
