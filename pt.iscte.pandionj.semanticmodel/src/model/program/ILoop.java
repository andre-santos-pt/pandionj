package model.program;

public interface ILoop extends IBlock, IControlStructure {

	boolean executeBlockFirst();

	IBreak addBreakStatement();

	IContinue addContinueStatement();

	
}
