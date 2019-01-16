package impl.program;

import model.program.IBlock;
import model.program.IExpression;
import model.program.ISelection;

class Selection extends Statement implements ISelection {
	private final IExpression guard;
	private final IBlock block;
	private final IBlock alternativeBlock;

	public Selection(Block parent, IExpression guard, IBlock block, IBlock alternativeBlock) {
		super(parent);
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

	@Override
	public String toString() {
		return "if " + guard + " " + block + (alternativeBlock != null ? "else " + alternativeBlock : "");
	}
}
