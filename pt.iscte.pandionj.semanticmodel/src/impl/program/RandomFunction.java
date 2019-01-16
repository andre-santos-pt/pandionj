package impl.program;

import impl.machine.Value;
import model.machine.ICallStack;
import model.program.ExecutionError;
import model.program.IDataType;

public class RandomFunction extends Procedure {

	public RandomFunction() {
		super("random", IDataType.DOUBLE);
	}
	
	@Override
	public boolean execute(ICallStack stack) throws ExecutionError {
		double d = Math.random();
		stack.getTopFrame().setReturn(Value.create(IDataType.DOUBLE, d));
		return true;
	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}
}
