package model.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;

public interface IStatement extends ISourceElement {
	IBlock getParent();

	boolean isControl();
	
	default IProcedure getProcedure() {
		IBlock b = getParent();
		while(b != null && !(b instanceof IProcedure))
			b = b.getParent();
		
		return (IProcedure) b;
	}
	
	default int getDepth() {
		int d = 1;
		IBlock b = getParent();
		while(b != null && !(b instanceof IProcedure)) {
			b = b.getParent();
			d++;
		}
		return d;
	}
	
	default List<ISemanticProblem> validateSematics() {
		return ImmutableList.of();
	}
	
	default boolean isValid() {
		return validateSematics().isEmpty();
	}
	
	// ARCH: only called my stack frame
	boolean execute(ICallStack stack) throws ExecutionError;
}
