package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IArrayElementExpression extends IVariableExpression {

	IArrayVariableDeclaration getVariable();
	
	List<IExpression> getIndexes(); // size() >= 1
	
	@Override
	default IValue evaluate(IStackFrame frame) throws ExecutionError {
		IValue variable = frame.getVariable(getVariable().getIdentifier());
		IValue element = variable;
		for(IExpression e : getIndexes()) {
			IValue i = frame.evaluate(e);
			int index = (int) i.getValue();
			if(index < 0)
				throw new ExecutionError(e, "negative array index", index);
			element = ((IArray) element).getElement(index);
		}
		return element;
	}
}
