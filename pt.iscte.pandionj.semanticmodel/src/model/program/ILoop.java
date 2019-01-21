package model.program;

public interface ILoop extends IControlStructure {

	boolean executeBlockFirst(); // TODO dowhile

	IBreak addBreakStatement();

	IContinue addContinueStatement();

	
}
