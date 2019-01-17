package model.program;

import model.machine.ICallStack;

public interface IContinue extends IStatement {
	@Override
	default boolean isControl() {
		return false;
	}

	@Override
	default boolean execute(ICallStack stack) throws ExecutionError {
		return true;
	}
}