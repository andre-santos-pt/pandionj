package model.machine;

import java.util.List;

import impl.machine.ExecutionError;

public interface IExecutable {

//	default IStatement getStatement() {
//		return (IStatement) this;
//	}
	
	// TODO to void?
	boolean execute(ICallStack stack, List<IValue> expressions) throws ExecutionError;
}
