package model.program;

import com.google.common.collect.ImmutableList;

import model.machine.IStackFrame;

public interface IProcedure extends IIdentifiableElement, IExecutable {
	ImmutableList<IVariableDeclaration> getParameters();	
	IBlock getBody();
	default boolean isDeclaration() {
		return getBody() == null;
	}
	
	ImmutableList<IVariableDeclaration> getVariables();
	boolean isFunction();
	boolean isRecursive();
	
	default boolean isSameAs(IProcedure procedure) {
		ImmutableList<IVariableDeclaration> selfParams = getParameters();
		ImmutableList<IVariableDeclaration> parameters = procedure.getParameters();

		if(!getIdentifier().equals(procedure.getIdentifier()) || selfParams.size() != parameters.size())
			return false;
		
		
		for(int i = 0; i < selfParams.size(); i++)
			if(!selfParams.get(i).equals(parameters.get(i)))
				return false;
		
		return true;
	}
	
	
	@Override
	default void execute(IStackFrame stack) {
		getBody().execute(stack);
	}
	
	//List<ISemanticError> getSemanticErrors();
}
