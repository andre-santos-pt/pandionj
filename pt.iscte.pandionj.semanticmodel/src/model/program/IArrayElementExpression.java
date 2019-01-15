package model.program;

import java.util.List;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;

public interface IArrayElementExpression extends IVariableExpression {

	IArrayVariableDeclaration getVariable();
	
	List<IExpression> getIndexes(); // size() >= 1
	
	@Override
	default IValue evaluate(ICallStack stack) throws ExecutionError {
		IStackFrame frame = stack.getTopFrame();
		IValue variable = frame.getVariable(getVariable().getId());
		IValue element = variable;
		for(IExpression e : getIndexes()) {
			IValue i = frame.evaluate(e);
			int index = ((Number) i.getValue()).intValue();
			if(index < 0)
				throw new ExecutionError(e, "negative array index", index);
			element = ((IArray) element).getElement(index);
		}
		return element;
	}
}
