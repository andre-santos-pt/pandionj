package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IArrayElementAssignment extends IVariableAssignment {
	List<IExpression> getIndexes(); // not null, length == getDimensions
	default int getDimensions() {
		return getIndexes().size();
	}
	
	IArrayVariableDeclaration getVariable();
	
	@Override
	default void execute(ICallStack callStack) throws ExecutionError {
		IStackFrame frame = callStack.getTopFrame();
		IArray array = (IArray) frame.getVariable(getVariable().getId());
		
		List<IExpression> indexes = getIndexes();
		IValue v = array;
		for(int i = 0; i < indexes.size()-1; i++) {
			int index = (int) frame.evaluate(indexes.get(i)).getValue();
			v = array.getElement(index);
		}
		int index = ((Number) frame.evaluate(indexes.get(indexes.size()-1)).getValue()).intValue();
		IValue value = frame.evaluate(getExpression());
		((IArray) v).setElement(index, value);
	}
}
