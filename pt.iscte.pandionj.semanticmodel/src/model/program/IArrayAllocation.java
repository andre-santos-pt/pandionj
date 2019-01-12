package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.IStackFrame;

public interface IArrayAllocation extends IExpression {
	List<IExpression> getDimensions();
	
	@Override
	default boolean isOperation() {
		return false;
	}
	
	default IArray evaluate(IStackFrame frame) throws ExecutionError {
		int dim = (int) frame.evaluate(getDimensions().get(0)).getValue();
		return frame.getArray(getType(), dim); // FIXME
	}
}
