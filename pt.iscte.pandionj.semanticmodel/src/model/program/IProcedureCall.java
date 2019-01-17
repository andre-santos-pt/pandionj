package model.program;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

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
	default boolean isControl() {
		return false;
	}
	
	@Override
	default List<ISemanticProblem> validateSematics() {
		if(getProcedure().getNumberOfParameters() != getArguments().size())
			return ImmutableList.of(ISemanticProblem.create("wrong number of arguments", this));
		
		// TODO param match
		
		return ImmutableList.of();
	}
	
	@Override
	default boolean execute(ICallStack callStack) throws ExecutionError {
		return executeCall(callStack, getProcedure(), getArguments(), this);
	}
	
	static boolean executeCall(ICallStack callStack, IProcedure procedure, List<IExpression> args, ISourceElement element) throws ExecutionError {
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
