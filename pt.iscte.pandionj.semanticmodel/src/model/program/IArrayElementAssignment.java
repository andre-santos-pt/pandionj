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
	
	@Override
	IArrayVariableDeclaration getVariable();
	
	@Override
	default void execute(ICallStack callStack) {
		IStackFrame frame = callStack.getTopFrame();
		IArray array = (IArray) frame.getVariable(getVariable().getIdentifier());
		
		List<IExpression> indexes = getIndexes();
		IValue v = array;
		for(int i = 0; i < indexes.size()-1; i++) {
			v = array.getElement((int) indexes.get(i).evaluate(frame).getValue());
		}
		((IArray) v).setElement((int) indexes.get(indexes.size()-1).evaluate(frame).getValue(), getExpression().evaluate(frame));
	}
}
