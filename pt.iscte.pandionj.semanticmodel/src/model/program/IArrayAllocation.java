package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.IStackFrame;

public interface IArrayAllocation extends IExpression {
	List<IExpression> getDimensions();
	
	default IArray evaluate(IStackFrame frame) {
		return frame.getArray(getType(), (int) getDimensions().get(0).evaluate(frame).getValue()); // FIXME
	}
}
