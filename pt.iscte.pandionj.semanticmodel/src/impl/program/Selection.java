package impl.program;

import model.program.IBlock;
import model.program.IExpression;
import model.program.ISelection;

class Selection extends Statement implements ISelection {
	private final IExpression guard;
	private final IBlock selectionBlock;
	private IBlock alternativeBlock;

	public Selection(Block parent, IExpression guard) {
		super(parent, true);
		assert parent != null;
		this.guard = guard;
		this.selectionBlock = parent.addLooseBlock();
		this.alternativeBlock = null;
	}

	@Override
	public IExpression getGuard() {
		return guard;
	}

	@Override
	public IBlock getSelectionBlock() {
		return selectionBlock;
	}

	@Override
	public IBlock getAlternativeBlock() {
		return alternativeBlock;
	}
	
	@Override
	public IBlock addAlternativeBlock() {
		if(alternativeBlock == null)
			alternativeBlock = ((Block) getParent()).addLooseBlock();
		return alternativeBlock;
	}

	@Override
	public String toString() {
		return "if " + guard + " " + selectionBlock + (alternativeBlock != null ? "else " + alternativeBlock : "");
	}
}
