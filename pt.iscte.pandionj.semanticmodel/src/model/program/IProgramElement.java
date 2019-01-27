package model.program;

public interface IProgramElement {
	void setProperty(String key, Object value);	
	
	Object getProperty(String key);
	
	default <T> void setProperty(Class<T> key, T value) {
		setProperty(key.getName(), value);
	}
	
	default <T> T getProperty(Class<T> key) {
		return (T) getProperty(key.getName());
	}
	default void setFlag(String key) {
		setProperty(key, Boolean.TRUE);
	}
	
	default boolean is(String key) {
		return Boolean.TRUE.equals(getProperty(key));
	}
}
