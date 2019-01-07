package model.program;

import model.machine.ICallStack;

public interface ILoop extends IConditionalStatement {
	
//	@Override
//	default String getDescription() {
//		return "Loop: " + getGuard().getSourceCode();
//	}
	
	@Override
	default void execute(ICallStack callStack) {
		while(getGuard().evaluate(callStack.getTopFrame()).equals(true)) {
			getBlock().execute(callStack);
		}
	}
}
