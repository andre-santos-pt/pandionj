package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IArrayLengthExpression extends IExpression {
	
	IArrayVariableDeclaration getVariable();

	List<IExpression> getIndexes(); // size() >= 1
	
	@Override
	default boolean isOperation() {
		return false;
	}
	
	@Override
	default IValue evaluate(IStackFrame frame) throws ExecutionError {
		IArray array = (IArray) frame.getVariable(getVariable().getIdentifier());
		
		List<IExpression> indexes = getIndexes();
		IValue v = array;
		for(int i = 0; i < indexes.size()-1; i++) {
			int index = (int) frame.evaluate(indexes.get(i)).getValue();
			v = array.getElement(index);
		}

		return frame.getValue(((IArray) v).getLength());
	}
}
