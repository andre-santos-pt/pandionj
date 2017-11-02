package pt.iscte.pandionj.extensibility;

import java.util.List;

import pt.iscte.pandionj.model.PrimitiveType;

public interface IVisibleMethod {

	String getName();

	List<String> getParameterTypes();
	
	String getReturnType();
	
	default int getNumberOfParameters() {
		return getParameterTypes().size();
	}
	
	default String getSignatureText() {
		return getName() + (getNumberOfParameters() == 0 ? "()" : "(...)");
	}
	
	default boolean isPrimitiveValue() {
		return PrimitiveType.isPrimitive(getReturnType());
	}
}
