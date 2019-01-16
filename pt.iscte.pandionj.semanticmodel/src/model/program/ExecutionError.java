package model.program;

public class ExecutionError extends Exception {
	enum Type {
		STACK_OVERFLOW, INFINTE_CYCLE, NULL_POINTER, ARRAY_INDEX_BOUNDS, NEGATIVE_ARRAY_SIZE, VALUE_OVERFLOW, OUT_OF_MEMORY;
	}
	
	private static final long serialVersionUID = 1L;

	private final Type type;
	private final ISourceElement element;
	private final String message;
	private final Object arg;
	
	public ExecutionError(Type type, ISourceElement element, String message) {
		this(type, element, message, null);
	}
	
	public ExecutionError(Type type, ISourceElement element, String message, Object arg) {
		assert element != null;
		assert message != null && !message.isEmpty();
		this.type = type;
		this.element = element;
		this.message = message;
		this.arg = arg;
	}
	
	public Type getType() {
		return type;
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
		return type + " at " + element + ": " + message + (arg != null ? ": " + arg : "");
	}
}
