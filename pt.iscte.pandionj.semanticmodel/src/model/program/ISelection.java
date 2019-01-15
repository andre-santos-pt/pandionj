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
	default boolean execute(ICallStack callStack) throws ExecutionError {
		IValue guard = callStack.evaluate(getGuard());
		if(guard.equals(IValue.TRUE)) {
			if(!callStack.execute(getBlock()))
				return false;
		}
		else if(hasAlternativeBlock()) {
			if(!callStack.execute(getAlternativeBlock()))
				return false;
		}
		return true;
	}
}
