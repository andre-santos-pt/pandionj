package model.program;

import model.machine.ICallStack;

public interface ISelection extends IConditionalStatement {
	// may be null
	IBlock getAlternativeBlock();
	
//	@Override
//	default String getDescription() {
//		return "Selection: " + getGuard().getSourceCode() + getAlternativeBlock() == null ? "" : " Alternative: " + getAlternativeBlock();
//	}
	
	default boolean hasAlternativeBlock() {
		return getAlternativeBlock() != null;
	}
	
	@Override
	default void execute(ICallStack callStack) {
		if(getGuard().evaluate(callStack.getTopFrame()).equals(true))
			getBlock().execute(callStack);
		else if(hasAlternativeBlock())
			getAlternativeBlock().execute(callStack);
		
	}
}
