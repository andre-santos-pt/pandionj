package pt.iscte.pandionj.extensibility;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import com.google.common.collect.Multimap;

public interface IObjectModel extends IEntityModel {
	
	String getStringValue();
	
	IArrayModel<?> getArray(String fieldName);
	int getInt(String fieldName);
	
	
	interface InvocationResult {
		default void valueReturn(Object o) { }
	}
	
	void invoke(String methodName, InvocationResult listener, String ... args);
	
	List<IMethod> getInstanceMethods();
	
	
	List<IMethod> getVisibleMethods();
	boolean hasAttributeTags();
	Multimap<String, String> getAttributeTags();
	IType getType();
	List<IVariableModel<?>> getFields();
	
}
