package model.program;

import model.machine.ICallStack;

public interface IExecutable {
	default void execute(ICallStack callStack) {  }
}
