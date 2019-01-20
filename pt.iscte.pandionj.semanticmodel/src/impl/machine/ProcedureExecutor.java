package impl.machine;

import java.util.Stack;

import model.machine.IValue;
import model.program.IProcedure;
import model.program.IProgramElement;

public class ProcedureExecutor {

	private Stack<BlockIterator> stack;
	
	public ProcedureExecutor(IProcedure procedure) {
		stack = new Stack<BlockIterator>();
		if(!procedure.isEmpty())
			stack.push(new BlockIterator(procedure));
	}
	
	public boolean isOver() {
		return stack.isEmpty();
	}
	
	public void moveNext(IValue last) throws ExecutionError {
		assert !isOver();
		
		if(stack.peek().hasNext()) {
			BlockIterator child = stack.peek().moveNext(last);
			if(child != null)
				stack.push(child);
		}
		while(!stack.isEmpty() && !stack.peek().hasNext())
			stack.pop();
	}
	
	public IProgramElement current() {
		assert !isOver();
		return stack.peek().current();
	}
	
	
}
