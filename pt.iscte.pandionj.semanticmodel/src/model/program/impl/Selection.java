package model.program.impl;

import model.program.IBlock;
import model.program.IExpression;
import model.program.ISelection;

public class Selection extends SourceElement implements ISelection {
	private final IExpression guard;
	private final IBlock block;
	private final IBlock alternativeBlock;

	public Selection(IExpression guard, IBlock block, IBlock alternativeBlock) {
		this.guard = guard;
		this.block = block;
		this.alternativeBlock = alternativeBlock;
	}

	@Override
	public IExpression getGuard() {
		return guard;
	}

	@Override
	public IBlock getBlock() {
		return block;
	}

	@Override
	public IBlock getAlternativeBlock() {
		return alternativeBlock;
	}

}
