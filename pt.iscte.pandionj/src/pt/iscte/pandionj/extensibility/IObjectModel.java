package pt.iscte.pandionj.extensibility;

import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.debug.core.IJavaValue;

public interface IObjectModel extends IEntityModel {

	int getInt(String fieldName);
	IArrayModel getArray(String fieldName);
	String getStringValue();
	void invoke3(String methodName, IJavaValue[] args, IWatchExpressionListener listener);
}
