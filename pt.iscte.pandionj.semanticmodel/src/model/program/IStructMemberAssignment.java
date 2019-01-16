package model.program;

import model.machine.ICallStack;

public interface IStructMemberAssignment extends IStatement {

	IStructType getStruct();
	String getMemberId();

	@Override
	default boolean execute(ICallStack callStack) throws ExecutionError {
		// TODO Auto-generated method stub
		return IStatement.super.execute(callStack);
	}
	
}
