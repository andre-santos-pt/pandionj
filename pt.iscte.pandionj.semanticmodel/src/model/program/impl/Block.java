package model.program.impl;

import com.google.common.collect.ImmutableList;

import model.program.IBlock;
import model.program.IStatement;

public class Block extends SourceElement implements IBlock{

	private final ImmutableList<IStatement> statements;

	public Block(ImmutableList<IStatement> statements) {
		this.statements = statements;
	}

	@Override
	public ImmutableList<IStatement> getStatements() {
		return statements;
	}

}
