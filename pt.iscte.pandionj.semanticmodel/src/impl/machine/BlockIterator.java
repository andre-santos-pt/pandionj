package impl.machine;

import java.util.List;

import model.machine.IValue;
import model.program.IBlock;
import model.program.IControlStructure;
import model.program.IExpression;
import model.program.IInstruction;
import model.program.ILoop;
import model.program.ISelection;
import model.program.ISelectionWithAlternative;
import model.program.IProgramElement;
import model.program.IStatement;

public class BlockIterator {
	private List<IInstruction> elements;
	private int i;
	private IExpression eval;
	
	public BlockIterator(IBlock root) {
		assert !root.isEmpty();
		this.elements = root.getInstructionSequence();
		this.i = 0;
		
		if(current() instanceof IControlStructure)
			eval = ((IControlStructure) current()).getGuard();
	}

	public boolean hasNext() {
		return i != elements.size();
	}

	public BlockIterator moveNext(IValue last) throws ExecutionError {
		assert hasNext();
		if(last != null)
			eval = null;
		
		IProgramElement current = elements.get(i);
		if(current instanceof IStatement) {
			i++;
		}
		else if(current instanceof IControlStructure && last == null) {
			eval = ((IControlStructure) current).getGuard();
			return null;
		}
		else if(current instanceof ISelection) {
			ISelection sel = (ISelection) current;
			i++;
			if(last.equals(IValue.TRUE)) {
				if(!sel.isEmpty())
					return new BlockIterator(sel);
			}
			else if(sel instanceof ISelectionWithAlternative) {
				IBlock elseBlock = ((ISelectionWithAlternative) sel).getAlternativeBlock();
				if(!elseBlock.isEmpty())
					return new BlockIterator(elseBlock);
			}
		}
		else if(current instanceof ILoop) {
			ILoop loop = (ILoop) current;
			if(last.equals(IValue.TRUE) && !loop.isEmpty())
				return new BlockIterator(loop);
			else
				i++;
		}

		return null;
	}

	public IProgramElement current() {
		assert i >= 0 && i < elements.size();
		return eval == null ? elements.get(i) : eval;
	}

	@Override
	public String toString() {
		return hasNext() ? current().toString() : "over";
	}
}