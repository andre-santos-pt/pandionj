package model.program.impl;

import model.program.IStatement;

abstract class Statement extends SourceElement implements IStatement {
	private final Block parent;
	
	public Statement(Block parent) {
		this.parent = parent;
		if(parent != null)
			parent.addStatement(this);
	}
	
	@Override
	public Block getParent() {
		return parent;
	}

}
