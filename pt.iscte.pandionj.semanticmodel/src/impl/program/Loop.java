package impl.program;

import java.util.List;

import impl.machine.ExecutionError;
import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IBreak;
import model.program.IContinue;
import model.program.IExpression;
import model.program.ILoop;

class Loop extends Block implements ILoop {
	private final IExpression guard;
	private final boolean executeBlockFirst;

	public Loop(Block parent, IExpression guard, boolean executeBlockFirst) {
		super(parent, true);
		this.guard = guard;
		this.executeBlockFirst = executeBlockFirst;
	}
	
	@Override
	public Block getParent() {
		return (Block) super.getParent();
	}
	
	@Override
	public IExpression getGuard() {
		return guard;
	}

	@Override
	public boolean executeBlockFirst() {
		return executeBlockFirst;
	}

	@Override
	public String toString() {
		return "while " + guard + " " + super.toString();
	}
	
	@Override
	public IBreak addBreakStatement() {
		return new Break(this);
	}
	
	@Override
	public IContinue addContinueStatement() {
		return new Continue(this);
	}
	
	private static class Break extends Statement implements IBreak {
		public Break(ILoop parent) {
			super(parent, true);
		}
		
		@Override
		public String toString() {
			return "break";
		}
		
		@Override
		public boolean execute(ICallStack stack, List<IValue> expressions) throws ExecutionError {
			return true;
		}
	}
	
	private static class Continue extends Statement implements IContinue {
		public Continue(ILoop parent) {
			super(parent, true);
		}
		
		@Override
		public String toString() {
			return "continue";
		}
		
		@Override
		public boolean execute(ICallStack stack, List<IValue> expressions) throws ExecutionError {
			return true;
		}
	}
}
