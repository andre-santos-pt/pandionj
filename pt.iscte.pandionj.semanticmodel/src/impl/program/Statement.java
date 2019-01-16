package impl.program;

import model.program.IBlock;
import model.program.IStatement;

abstract class Statement extends SourceElement implements IStatement {
	private final IBlock parent;
	
	public Statement(IBlock parent) {
		this.parent = parent;
		if(parent != null)
			parent.addStatement(this);
	}
	
	@Override
	public IBlock getParent() {
		return parent;
	}

}
