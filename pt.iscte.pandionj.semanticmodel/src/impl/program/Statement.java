package impl.program;

import model.machine.IExecutable;
import model.program.IBlock;
import model.program.IStatement;

abstract class Statement extends ProgramElement implements IStatement, IExecutable {
	private final IBlock parent;
	
	public Statement(IBlock parent, boolean addToParent) {
		this.parent = parent;
		if(parent != null && addToParent)
			((Block) parent).addStatement(this);
	}
	
	@Override
	public IBlock getParent() {
		return parent;
	}

}
