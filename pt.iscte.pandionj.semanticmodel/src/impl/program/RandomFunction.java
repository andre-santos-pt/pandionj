package impl.program;

import model.program.IDataType;

// TODO
public class RandomFunction extends Procedure {

	public RandomFunction() {
		super("random", IDataType.DOUBLE);
	}
	
//	@Override
//	public boolean execute(ICallStack stack) throws ExecutionError {
//		double d = Math.random();
//		stack.getTopFrame().setReturn(Value.create(IDataType.DOUBLE, d));
//		return true;
//	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}
}
