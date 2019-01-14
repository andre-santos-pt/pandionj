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
		int dim = ((Number) frame.evaluate(getDimensions().get(0)).getValue()).intValue();
		return frame.getArray(getType(), dim); // FIXME
	}
}
