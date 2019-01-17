package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface ISelection extends IStatement {
	IExpression getGuard(); // not null
	IBlock getSelectionBlock(); // not null
	IBlock getAlternativeBlock(); // may be null
	IBlock addAlternativeBlock(); // create
	
	default boolean hasAlternativeBlock() {
		return getAlternativeBlock() != null;
	}
	
	@Override
	default boolean isControl() {
		return true;
	}
	
	@Override
	default boolean execute(ICallStack callStack) throws ExecutionError {
		IValue guard = callStack.evaluate(getGuard());
		if(guard.equals(IValue.TRUE)) {
			if(!callStack.execute(getSelectionBlock()))
				return false;
		}
		else if(hasAlternativeBlock()) {
			if(!callStack.execute(getAlternativeBlock()))
				return false;
		}
		return true;
	}
}
