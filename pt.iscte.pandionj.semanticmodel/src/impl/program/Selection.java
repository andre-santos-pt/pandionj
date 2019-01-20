package impl.program;

import model.program.IBlock;
import model.program.IExpression;
import model.program.ISelection;

class Selection extends ProgramElement implements ISelection {
	private final Block parent;
	private final IExpression guard;
	private final IBlock selectionBlock;
	private IBlock alternativeBlock;

	public Selection(Block parent, IExpression guard) {
		assert parent != null;
		this.parent = parent;
		parent.addStatement(this);
		this.guard = guard;
		this.selectionBlock = parent.addLooseBlock();
		this.alternativeBlock = null;
	}

	@Override
	public IBlock getParent() {
		return parent;
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
