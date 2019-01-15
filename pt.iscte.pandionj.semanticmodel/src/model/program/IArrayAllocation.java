package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;

public interface IArrayAllocation extends IExpression {
	List<IExpression> getDimensions();
	
	@Override
	default boolean isOperation() {
		return false;
	}
	
	@Override
	default IArray evaluate(ICallStack stack) throws ExecutionError {
		IStackFrame frame = stack.getTopFrame();
		List<IExpression> dimensions = getDimensions();
		int[] dims = new int[dimensions.size()];
		for(int i = 0; i < dims.length; i++)
			dims[i] = ((Number) frame.evaluate(dimensions.get(i)).getValue()).intValue();

		IArray array = frame.getArray(getType(), dims); 
		return array;
	}
}
