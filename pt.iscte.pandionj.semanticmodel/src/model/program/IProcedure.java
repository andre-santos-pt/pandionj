package model.program;

import java.util.List;

/**
 * Mutable
 */
public interface IProcedure extends IProcedureDeclaration {
	
	List<IVariableDeclaration> getLocalVariables();
	List<IVariableDeclaration> getVariables();
	IVariableDeclaration getVariable(String id);
	IDataType getReturnType();
	
	IBlock getBody();
	
	default boolean isBuiltIn() {
		return false;
	}
	
}
