package model.program;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;

public interface IBlock extends ISourceElement, IExecutable, IStatement, Iterable<IStatement> {
	IBlock getParent();
	ImmutableList<IStatement> getStatements();
	
	default boolean isEmpty() {
		return getStatements().isEmpty();
	}
	
	@Override
	default void execute(ICallStack stack) {
		for(IStatement s : getStatements())
			stack.getTopFrame().execute(s);
	}
	
	@Override
	default Iterator<IStatement> iterator() {
		return getStatements().iterator();
	}
}
