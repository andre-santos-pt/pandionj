package model.program;

import java.util.ArrayList;
import java.util.List;

import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IProcedureCall extends IStatement {
	IProcedure getProcedure();
	List<IExpression> getArguments();

	default boolean isOperation() {
		return false;
	}
	
	
	@Override
	default void execute(ICallStack callStack) throws ExecutionError {
		executeCall(callStack, getProcedure(), getArguments());
	}
	
	static void executeCall(ICallStack callStack, IProcedure procedure, List<IExpression> args) throws ExecutionError {
		List<IValue> argsValues = new ArrayList<>();
		for(int i = 0; i < procedure.getNumberOfParameters(); i++) {
			IValue arg = callStack.evaluate(args.get(i));
			argsValues.add(arg);
		}
		IStackFrame newFrame = callStack.newFrame(procedure, argsValues);
		
		newFrame.execute(procedure);
		IValue returnValue = newFrame.getReturn();
		callStack.terminateTopFrame(returnValue);
	}
	
//	List<IValue> args = new ArrayList<>();
//	for(int i = 0; i < getProcedure().getNumberOfParameters(); i++) {
//		IValue arg = callStack.evaluate(getArguments().get(i));
//		args.add(arg);
//	}
//	IStackFrame newFrame = callStack.newFrame(getProcedure(), args);
//	
//	newFrame.execute(getProcedure());
//	IValue returnValue = newFrame.getReturn();
//	callStack.terminateTopFrame(returnValue);
	
//	@Override
//	default IValue evaluate(IStackFrame stackFrame) throws ExecutionError {
//		stackFrame.execute(this);
//		IStackFrame last = stackFrame.getCallStack().getLastTerminatedFrame();
//		return last.getReturn();
//	}
}
