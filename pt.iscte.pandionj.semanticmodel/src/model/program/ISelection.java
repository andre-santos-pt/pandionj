package model.program;

import model.machine.IStackFrame;

public interface ISelection extends IConditionalStatement {
	// may be null
	IBlock getAlternativeBlock();
	
//	@Override
//	default String getDescription() {
//		return "Selection: " + getGuard().getSourceCode() + getAlternativeBlock() == null ? "" : " Alternative: " + getAlternativeBlock();
//	}
	
	@Override
	default void execute(IStackFrame stackFrame) {
		if(getGuard().evaluate(stackFrame).equals(true)) {
			getBlock().execute(stackFrame);
		}
		else {
			IBlock alternativeBlock = getAlternativeBlock();
			if(alternativeBlock != null)
				alternativeBlock.execute(stackFrame);
		}
	}
}
