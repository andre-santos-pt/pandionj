package model.program;

import model.machine.IStackFrame;

public interface ILoop extends IConditionalStatement {
	
//	@Override
//	default String getDescription() {
//		return "Loop: " + getGuard().getSourceCode();
//	}
	
	@Override
	default void execute(IStackFrame frame) {
		while(getGuard().evaluate(frame).equals(true)) {
			getBlock().execute(frame);
		}
	}
}
