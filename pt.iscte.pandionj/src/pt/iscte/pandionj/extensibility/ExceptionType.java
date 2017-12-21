package pt.iscte.pandionj.extensibility;

public enum ExceptionType {
	ARRAY_INDEX_OUT_BOUNDS(ArrayIndexOutOfBoundsException.class), 
	NULL_POINTER(NullPointerException.class);
	
	private String exceptionType;
	
	private ExceptionType(Class<? extends Exception> exceptionType) {
		this.exceptionType = exceptionType.getName();
	}
	
	public static ExceptionType match(String exceptionType) {
		for (ExceptionType e : values()) {
			if(e.exceptionType.equals(exceptionType))
				return e;
		}
		return null;
	}
}
