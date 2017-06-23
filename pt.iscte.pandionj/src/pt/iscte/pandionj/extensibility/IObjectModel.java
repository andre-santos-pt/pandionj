package pt.iscte.pandionj.extensibility;

import java.util.List;

import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.jdt.debug.core.IJavaValue;

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
