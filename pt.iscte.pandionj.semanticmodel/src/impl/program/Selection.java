package impl.program;

import model.program.IExpression;
import model.program.ISelection;

class Selection extends Block implements ISelection {
//	private final Block parent;
	private final IExpression guard;
//	private final IBlock selectionBlock;
	
	public Selection(Block parent, IExpression guard) {
		super(parent, true);
		assert parent != null;
		assert guard != null;
		this.guard = guard;
//		this.parent = parent;
//		parent.addStatement(this);
//		this.selectionBlock = parent.addLooseBlock();
	}

	@Override
	public Block getParent() {
		return (Block) super.getParent();
	}
	
	@Override
	public IExpression getGuard() {
		return guard;
	}

//	@Override
//	public IBlock getSelectionBlock() {
//		return selectionBlock;
//	}

	@Override
	public String toString() {
		return "if " + guard + " " + super.toString();
	}
}
