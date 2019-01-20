package impl.program;

import model.program.IBlock;
import model.program.IControlStructure;

abstract class ControlStructure extends ProgramElement implements IControlStructure {
	private final IBlock parent;
	
	public ControlStructure(IBlock parent) {
		this.parent = parent;
		if(parent != null)
			((Block) parent).addStatement(this);
	}
	
	@Override
	public IBlock getParent() {
		return parent;
	}

}
