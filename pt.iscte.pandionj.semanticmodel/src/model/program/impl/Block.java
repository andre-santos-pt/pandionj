package model.program.impl;

import com.google.common.collect.ImmutableList;

import model.program.IBlock;
import model.program.IStatement;

public class Block extends SourceElement implements IBlock{

	private final IBlock parent;
	private final ImmutableList<IStatement> statements;

	// for method
//	public Block(ImmutableList<IStatement> statements) {
//		this.parent = null;
//		this.statements = statements;
//	}
	
	public Block(IBlock parent, ImmutableList<IStatement> statements) {
		this.parent = parent;
		this.statements = statements;
	}

	@Override
	public IBlock getParent() {
		return parent;
	}
	
	@Override
	public ImmutableList<IStatement> getStatements() {
		return statements;
	}

	

}
