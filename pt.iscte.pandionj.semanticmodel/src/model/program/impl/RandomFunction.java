package model.program.impl;

import model.machine.ICallStack;
import model.machine.impl.Value;
import model.program.ExecutionError;
import model.program.IDataType;

public class RandomFunction extends Procedure {

	public RandomFunction() {
		super("random", IDataType.DOUBLE);
	}
	
	@Override
	public void execute(ICallStack stack) throws ExecutionError {
		double d = Math.random();
		stack.getTopFrame().setReturn(Value.create(IDataType.DOUBLE, d));
	}

}
