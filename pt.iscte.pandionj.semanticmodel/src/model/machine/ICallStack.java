package model.machine;

import java.util.List;

import model.program.ExecutionError;
import model.program.IExpression;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IStatement;

public interface ICallStack {
	int getSize();
	
	IProgramState getProgramState();
	
	List<IStackFrame> getFrames();
	IStackFrame getTopFrame();
	
	IStackFrame newFrame(IProcedure procedure, List<IValue> args);
	
	void terminateTopFrame(IValue returnValue);
	
	
	default void execute(IStatement statement) throws ExecutionError {
		getTopFrame().execute(statement);
	}
	
	default IValue evaluate(IExpression expression) throws ExecutionError {
		if(expression instanceof ILiteral)
			return getProgramState().getValue(((ILiteral) expression).getStringValue());
		else
			return getTopFrame().evaluate(expression);
	}
	
	interface IListener {
		default void stackFrameCreated(IStackFrame stackFrame) {
			
		}
		default void stackFrameTerminated(IStackFrame stackFrame, IValue returnValue) {
			
		}
	}

	void addListener(IListener listener);
	
}
