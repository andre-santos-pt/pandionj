package model.program;

import model.machine.IStackFrame;

public interface IExecutable {
	default void execute(IStackFrame stackFrame) {  }
}
