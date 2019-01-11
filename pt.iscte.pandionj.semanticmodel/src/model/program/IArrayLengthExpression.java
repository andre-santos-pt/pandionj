package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IArrayLengthExpression extends IExpression {
	
	IArrayVariableDeclaration getVariable();

	List<IExpression> getIndexes(); // size() >= 1
	
	@Override
	default IValue evaluate(IStackFrame frame) {
		IArray array = (IArray) frame.getVariable(getVariable().getIdentifier());
		
		List<IExpression> indexes = getIndexes();
		IValue v = array;
		for(int i = 0; i < indexes.size()-1; i++) {
			v = array.getElement((int) indexes.get(i).evaluate(frame).getValue());
		}

		return frame.getValue(((IArray) v).getLength());
	}
}
