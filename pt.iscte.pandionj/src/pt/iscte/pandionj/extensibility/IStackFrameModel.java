package pt.iscte.pandionj.extensibility;

import java.util.Collection;

import org.eclipse.core.resources.IFile;

import pt.iscte.pandionj.model.RuntimeModel;

public interface IStackFrameModel extends IObservableModel<IStackFrameModel.StackEvent<?>> {

	Collection<IVariableModel<?>> getStackVariables();
	Collection<IVariableModel<?>> getAllVariables();
	Collection<IReferenceModel> getReferencesTo(IEntityModel e);
	RuntimeModel getRuntime();
	String getInvocationExpression();
	
	class StackEvent<T> {
		public enum Type {
			NEW_VARIABLE, VARIABLE_OUT_OF_SCOPE, EXCEPTION;
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
	int getLineNumber();
	boolean exceptionOccurred();
	String getExceptionType();
	IFile getSourceFile();
}
