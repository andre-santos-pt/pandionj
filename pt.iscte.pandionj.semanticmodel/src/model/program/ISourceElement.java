package model.program;

public interface ISourceElement {
	String getSourceCode();
	int getOffset();
	int getLength();
	int getLine();
}
