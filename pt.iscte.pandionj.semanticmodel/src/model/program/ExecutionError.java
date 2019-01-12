package model.program;

public class ExecutionError extends Exception {
	// TODO
	enum Type {
		ARRAY_INDEX_BOUNDS, NEGATIVE_ARRAY_SIZE, NULL_POINTER;
	}
	
	private static final long serialVersionUID = 1L;

	private final ISourceElement element;
	private final String message;
	private Object arg;
	
	public ExecutionError(ISourceElement element, String message, Object arg) {
		assert element != null;
		assert message != null && !message.isEmpty();
		this.element = element;
		this.message = message;
		this.arg = arg;
	}
	
	public ISourceElement getSourceElement() {
		return element;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Object getArgument() {
		return arg;
	}
	
	@Override
	public String toString() {
		return element + ": " + message + ": " + arg;
	}
}
