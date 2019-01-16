package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;

public interface IExecutable {
	
	default List<ISemanticProblem> validateSematics() {
		return ImmutableList.of();
	}
	
	default boolean isValid() {
		return validateSematics().isEmpty();
	}
	
	// ARCH: only called my stack frame
	default boolean execute(ICallStack callStack) throws ExecutionError { 
		return true;
	}
}
