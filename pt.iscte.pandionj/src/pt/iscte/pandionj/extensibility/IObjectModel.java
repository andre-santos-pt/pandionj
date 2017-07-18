package pt.iscte.pandionj.extensibility;

import java.util.List;

public interface IObjectModel extends IObservableModel {
	String getTypeName();
	IArrayModel getArray(String fieldName);
	String getStringValue();
	
	int getInt(String fieldName);
	
	
	// ?
	interface InvocationResult {
		default void valueReturn(Object o) { }
	}
	
	void invoke(String methodName, InvocationResult listener, String ... args);
	
	
	String toStringValue();
	Object getInstanceMethods();
	
	
	List<IVisibleMethod> getVisibleMethods();
	
}
