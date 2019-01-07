package model.program;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IProcedureCall extends IExpression, IStatement {
	IProcedure getProcedure();
	List<IExpression> getArguments();
	
	@Override
	default void execute(ICallStack callStack) {
		List<IValue> args = new ArrayList<>();
		ImmutableList<IVariableDeclaration> parameters = getProcedure().getParameters();
		for(int i = 0; i < parameters.size(); i++)
			args.add(getArguments().get(i).evaluate(callStack.getTopFrame()));
		
		IStackFrame newFrame = callStack.newFrame(getProcedure(), args);
		
		getProcedure().execute(callStack);
		IValue returnValue = newFrame.getReturn();
		callStack.terminateTopFrame(returnValue);
	}
	
	@Override
	default IValue evaluate(IStackFrame stackFrame) {
		execute(stackFrame.getCallStack());
		return stackFrame.getReturn();
	}
}
