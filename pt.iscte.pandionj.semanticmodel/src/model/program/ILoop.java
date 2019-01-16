package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface ILoop extends IConditionalStatement, IBlock {

	boolean executeBlockFirst();

	@Override
	default boolean execute(ICallStack callStack) throws ExecutionError {
		int maxIt = callStack.getProgramState().getLoopIterationMaximum();
		int c = 0;
		if(executeBlockFirst()) {
			Object s = Util.executeLoopBlock(getBlock(), callStack);
			if(s instanceof IBreak)
				return true;
			else if(Boolean.FALSE.equals(s))
				return false;
			c++;
		}
		while(callStack.evaluate(getGuard()).equals(IValue.TRUE)) {
			Object s = Util.executeLoopBlock(getBlock(), callStack);
			if(s instanceof IBreak)
				return true;
			else if(Boolean.FALSE.equals(s))
				return false;
			c++;
			if(c == maxIt)
				throw new ExecutionError(ExecutionError.Type.INFINTE_CYCLE, this, "loop exceeded limit", c);
		}
		return true;
	}

	class Util {
		private static Object executeLoopBlock(IBlock block, ICallStack callStack) throws ExecutionError {
			for(IStatement s : block) {
				if(s instanceof IBreak || s instanceof IContinue)
					return s;
				
				if(!callStack.getTopFrame().execute(s))
					return false; // in case of return
			}
			return null;
		}
	}

	IBreak breakStatement();

	IContinue continueStatement();

	public interface IBreak extends IStatement { }

	public interface IContinue extends IStatement { }
}
