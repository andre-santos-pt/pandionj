package pt.iscte.pandionj.extensibility;

import java.util.Collection;

import pt.iscte.pandionj.model.RuntimeModel;

public interface IStackFrameModel extends IObservableModel<IStackFrameModel.StackEvent> {

	Collection<IVariableModel<?>> getStackVariables();
	Collection<IVariableModel<?>> getAllVariables();
	Collection<IReferenceModel> getReferencesTo(IEntityModel e);
	RuntimeModel getRuntime();
	String getInvocationExpression();
	
	class StackEvent {
		public enum Type {
			NEW_VARIABLE, VARIABLE_OUT_OF_SCOPE;
		}
		public final Type type;
		public final IVariableModel<?> variable;

		public StackEvent(Type type, IVariableModel<?> variable) {
			this.type = type;
			this.variable = variable;
		}
	}

	boolean isObsolete();
	boolean isExecutionFrame();
	int getLineNumber();
}
