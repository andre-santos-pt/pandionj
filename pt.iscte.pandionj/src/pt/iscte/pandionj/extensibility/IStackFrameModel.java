package pt.iscte.pandionj.extensibility;

import java.util.Collection;

import pt.iscte.pandionj.model.RuntimeModel;

public interface IStackFrameModel extends IObservableModel {

	Collection<IVariableModel> getStackVariables();
	Collection<IVariableModel> getAllVariables();
	Collection<IReferenceModel> getReferencesTo(IEntityModel e);
	RuntimeModel getRuntime();

//	interface StackListener {
//		void newVar(IVariableModel var);
//		void outOfScopeVar(IVariableModel var);
//	}
	
	
}
