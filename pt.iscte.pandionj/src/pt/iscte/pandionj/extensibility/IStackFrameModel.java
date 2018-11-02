package pt.iscte.pandionj.extensibility;

import java.util.Collection;

import org.eclipse.core.resources.IFile;

import pt.iscte.pandionj.model.RuntimeModel;
import pt.iscte.pandionj.parser.VariableInfo;

public interface IStackFrameModel extends IObservableModel<IStackFrameModel.StackEvent<?>> {

	Collection<IVariableModel> getAllVariables();
	Iterable<IVariableModel> getLocalVariables();
	Iterable<IReferenceModel> getReferenceVariables();
	Iterable<IReferenceModel> getReferencesTo(IEntityModel e);
	RuntimeModel getRuntime();
	String getInvocationExpression();
	
	class StackEvent<T> {
		public enum Type {
			NEW_VARIABLE, VARIABLE_OUT_OF_SCOPE, EXCEPTION, ARRAY_INDEX_EXCEPTION, STEP, RETURN_VALUE;
		}
		public final Type type;
		public final T arg;

		public StackEvent(Type type, T arg) {
			this.type = type;
			this.arg = arg;
		}
	}

	boolean isObsolete();
	boolean isExecutionFrame();
	boolean isInstance();
//	IObjectModel getThis();
	boolean isInstanceFrameOf(IObjectModel model);
	int getLineNumber();
	boolean exceptionOccurred();
	String getExceptionType();
	StackEvent<String> getExceptionEvent();
	IFile getSourceFile();
	
	VariableInfo getVariableInfo(String varName, boolean isField);
}
