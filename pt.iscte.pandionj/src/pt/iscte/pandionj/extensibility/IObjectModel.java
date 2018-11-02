package pt.iscte.pandionj.extensibility;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

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
	Map<String, ITag> getAttributeTags();
	IType getType();
	List<IVariableModel> getFields();
	
	default boolean isToStringDefined() {
		for(IMethod m : getVisibleMethods())
			if(m.getElementName().equals("toString") && m.getParameterTypes().length == 0)
				return true;
		
		return false;
	}
	
}
