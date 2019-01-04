package model.program;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import model.machine.IStackFrame;
import model.machine.IValue;

public interface IProcedureCall extends IExpression, IStatement {
	IProcedure getProcedure();
	IExpression getArguments();
	
	@Override
	default void execute(IStackFrame stackFrame) {
		getProcedure().execute(stackFrame);
	}
	
	@Override
	default IValue evaluate(IStackFrame stackFrame) {
		Map<IVariableDeclaration, IValue> args = new HashMap<>();
		ImmutableList<IVariableDeclaration> parameters = getProcedure().getParameters();
		for(int i = 0; i < parameters.size(); i++)
			args.put(parameters.get(i), getArguments().evaluate(stackFrame));
		IStackFrame newFrame = stackFrame.newChildFrame(args);
		execute(newFrame);
		IValue returnValue = newFrame.getReturn();
		stackFrame.terminateFrame(returnValue);
		return returnValue;
	}
}
