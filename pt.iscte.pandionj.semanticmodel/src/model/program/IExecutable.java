package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;

public interface IExecutable {
	
	default List<IProblem> validate() {
		return ImmutableList.of();
	}
	
	default boolean isValid() {
		return validate().isEmpty();
	}
	
	// ARCH: only called my stack frame
	default boolean execute(ICallStack callStack) throws ExecutionError { 
		return true;
	}
}
