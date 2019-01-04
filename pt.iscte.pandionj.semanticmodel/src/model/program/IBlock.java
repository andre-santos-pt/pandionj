package model.program;

import com.google.common.collect.ImmutableList;

import model.machine.IStackFrame;

public interface IBlock extends ISourceElement, IExecutable, IStatement {
	ImmutableList<IStatement> getStatements();
	
	default boolean isEmpty() {
		return getStatements().isEmpty();
	}
	
	@Override
	default void execute(IStackFrame stack) {
		for(IStatement s : getStatements())
			s.execute(stack);
	}
}
