package pt.iscte.pandionj.extensibility;

import java.util.List;

import org.eclipse.jdt.core.IType;

import com.google.common.collect.Multimap;

public interface IObjectModel extends IEntityModel {
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
	boolean hasAttributeTags();
	Multimap<String, String> getAttributeTags();
	IType getType();
	
}
