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
	
	default void execute(ICallStack callStack) {  }
}
