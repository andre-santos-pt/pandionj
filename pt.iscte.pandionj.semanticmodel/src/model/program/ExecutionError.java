package model.program;

public abstract class ExecutionError extends RuntimeException {
	public abstract ISourceElement getSourceElement();
	public abstract String getMessage();
}
