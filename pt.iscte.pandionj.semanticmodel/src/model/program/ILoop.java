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
	default void execute(ICallStack callStack) {
		if(executeBlockFirst())
			if(!executeLoopBlock(getBlock(), callStack))
				return;

		while(getGuard().evaluate(callStack.getTopFrame()).equals(IValue.TRUE)) {
			if(!executeLoopBlock(getBlock(), callStack))
				return;
		}
	}

	public static boolean executeLoopBlock(IBlock block, ICallStack callStack) {
		for(IStatement s : block) {
			if(s instanceof IBreak)
				return false;
			else if(s instanceof IContinue)
				return true;
			else 
				s.execute(callStack);
		}
		return true;
	}

	IBreak breakStatement();

	IContinue continueStatement();
	
	public interface IBreak extends IStatement {

	}


	public interface IContinue extends IStatement {
		
	}

}
