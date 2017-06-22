package pt.iscte.pandionj.extensibility;

import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.jdt.debug.core.IJavaValue;

public interface IObjectModel extends IEntityModel {
	IJavaValue[] NO_ARGS = new IJavaValue[0];
	
	IArrayModel getArray(String fieldName);
	String getStringValue();
	
	int getInt(String fieldName);
	// ?
	interface InvocationResult {
		void valueReturn(Object o);
	}
	
	void invoke(String methodName, IWatchExpressionListener listener, IJavaValue[] args);
	
	
	String toStringValue();
	
	
	
}
