package model.program;

import model.machine.ICallStack;
import model.machine.IValue;

public interface ILoop extends IConditionalStatement, IBlock {

	//	@Override
	//	default String getDescription() {
	//		return "Loop: " + getGuard().getSourceCode();
	//	}

	boolean executeBlockFirst();

	@Override
	default void execute(ICallStack callStack) throws ExecutionError {
		if(executeBlockFirst())
			if(!executeLoopBlock(getBlock(), callStack))
				return;

		while(callStack.evaluate(getGuard()).equals(IValue.TRUE)) {
			if(!executeLoopBlock(getBlock(), callStack))
				return;
		}
	}

	public static boolean executeLoopBlock(IBlock block, ICallStack callStack) throws ExecutionError {
		for(IStatement s : block) {
			if(s instanceof IBreak)
				return false;
			else if(s instanceof IContinue)
				return true;
			else 
				callStack.getTopFrame().execute(s);
		}
		return true;
	}

	IBreak breakStatement();

	IContinue continueStatement();
	
	public interface IBreak extends IStatement { }

	public interface IContinue extends IStatement { }
}
