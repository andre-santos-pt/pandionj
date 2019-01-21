package model.program;

public interface IProgramElement {
	void setProperty(String key, Object value);
	Object getProperty(String key);
	
	default void setFlag(String key) {
		setProperty(key, Boolean.TRUE);
	}
	
	default boolean is(String key) {
		return Boolean.TRUE.equals(getProperty(key));
	}
}
