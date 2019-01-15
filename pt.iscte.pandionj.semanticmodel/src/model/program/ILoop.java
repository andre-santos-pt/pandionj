package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface ILoop extends IConditionalStatement, IBlock {

	boolean executeBlockFirst();

	int getLoopLimit(); // TODO to Program

	@Override
	default boolean execute(ICallStack callStack) throws ExecutionError {
		if(executeBlockFirst()) {
			IStatement s = Util.executeLoopBlock(getBlock(), callStack);
			if(s instanceof IBreak)
				return true;
			else if(s instanceof IReturn)
				return false;
			//			if(!executeLoopBlock(getBlock(), callStack))
			//				return true;
		}
		int c = 0;
		while(callStack.evaluate(getGuard()).equals(IValue.TRUE)) {
			IStatement s = Util.executeLoopBlock(getBlock(), callStack);
			if(s instanceof IBreak)
				return true;
			else if(s instanceof IReturn)
				return false;

			System.out.println(getGuard());
			//			if(!executeLoopBlock(getBlock(), callStack))
			//				return true;
			c++;
			if(c == getLoopLimit())
				throw new ExecutionError(this, "loop exceeded limit", c);
		}
		return true;
	}

	class Util {
		private static IStatement executeLoopBlock(IBlock block, ICallStack callStack) throws ExecutionError {
			for(IStatement s : block) {
				if(s instanceof IBreak || s instanceof IContinue)
					return s;
				//			else if(s instanceof IContinue)
				//				return true;
				//			else 
				
				if(!callStack.getTopFrame().execute(s))
					return s;
			}
			return null;
		}
	}

	IBreak breakStatement();

	IContinue continueStatement();

	public interface IBreak extends IStatement { }

	public interface IContinue extends IStatement { }
}
