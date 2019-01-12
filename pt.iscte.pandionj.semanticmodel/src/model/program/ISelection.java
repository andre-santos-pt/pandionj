package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface ISelection extends IConditionalStatement {
	// may be null
	IBlock getAlternativeBlock();
	
	default boolean hasAlternativeBlock() {
		return getAlternativeBlock() != null;
	}
	
	@Override
	default void execute(ICallStack callStack) throws ExecutionError {
		IValue guard = callStack.evaluate(getGuard());
		if(guard.equals(IValue.TRUE))
			callStack.execute(getBlock());
		else if(hasAlternativeBlock())
			callStack.execute(getAlternativeBlock());
	}
}
