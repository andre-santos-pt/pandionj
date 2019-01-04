package model.program;

public interface IDataType extends IIdentifiableElement {
	boolean matches(Object object);
	Object match(String literal);
	
}
