package model.program;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;

public interface IProcedure extends IIdentifiableElement, IExecutable, IBlock {
	ImmutableList<IVariableDeclaration> getParameters();	
	IBlock getBody();
	void setBody(IBlock body);
	
//	default boolean isDeclaration() {
//		return getBody() == null;
//	}
	
	ImmutableList<IVariableDeclaration> getVariables();
	boolean isFunction();
	boolean isRecursive();
	
	default boolean hasSameSignature(IProcedure procedure) {
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
	default void execute(ICallStack stack) {
		for(IStatement s : this)
			stack.getTopFrame().execute(s);
//		getBody().execute(stack);
//		System.out.println(toString() + " -> " + stack.getReturn());
	}
	
	//List<ISemanticError> getSemanticErrors();
}
