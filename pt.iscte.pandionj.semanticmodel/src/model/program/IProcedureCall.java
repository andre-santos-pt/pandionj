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
	default boolean execute(ICallStack callStack) throws ExecutionError {
		return executeCall(callStack, getProcedure(), getArguments(), this);
	}
	
	static boolean executeCall(ICallStack callStack, IProcedure procedure, List<IExpression> args, ISourceElement element) throws ExecutionError {
		if(procedure.getNumberOfParameters() != args.size())
			throw new ExecutionError(element, "wrong number of arguments");
		
		List<IValue> argsValues = new ArrayList<>();
		for(int i = 0; i < procedure.getNumberOfParameters(); i++) {
			IValue arg = callStack.evaluate(args.get(i));
			argsValues.add(arg);
		}
		IStackFrame newFrame = callStack.newFrame(procedure, argsValues);
		
		boolean result = newFrame.execute(procedure);
		IValue returnValue = newFrame.getReturn();
		callStack.terminateTopFrame(returnValue);
		return result;
	}
}
