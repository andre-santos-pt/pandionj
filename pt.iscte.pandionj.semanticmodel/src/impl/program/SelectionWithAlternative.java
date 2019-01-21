package impl.program;

import model.program.IBlock;
import model.program.IExpression;
import model.program.ISelectionWithAlternative;

public class SelectionWithAlternative extends Selection implements ISelectionWithAlternative {
	private final IBlock alternativeBlock;
	
	public SelectionWithAlternative(Block parent, IExpression guard) {
		super(parent, guard);
		alternativeBlock = parent.addLooseBlock();
	}
	
	@Override
	public IBlock getAlternativeBlock() {
		return alternativeBlock;
	}
	
	@Override
	public String toString() {
		return super.toString() + " else " + alternativeBlock;
	}
}
