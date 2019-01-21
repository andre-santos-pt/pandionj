package model.program;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Mutable
 */
public interface IProcedure extends IIdentifiableElement {
	
	Iterable<IVariableDeclaration> getParameters();	
	int getNumberOfParameters();
	Iterable<IVariableDeclaration> getVariables(boolean includingParams);
	IVariableDeclaration getVariable(String id);
	IDataType getReturnType();
	
	IBlock getBody();
	
	default boolean isBuiltIn() {
		return false;
	}
	
	IVariableDeclaration addParameter(String id, IDataType type, Set<IVariableDeclaration.Flag> flags);
	
	default IVariableDeclaration addParameter(String id, IDataType type) {
		return addParameter(id, type, ImmutableSet.of());
	}
	
	// compares id and types of parameters
	// excludes return
	default boolean hasSameSignature(IProcedure procedure) {
		if(!getId().equals(procedure.getId()) || getNumberOfParameters() != procedure.getNumberOfParameters())
			return false;
		
		Iterator<IVariableDeclaration> procParamsIt = procedure.getParameters().iterator();
		for(IVariableDeclaration p : getParameters())
			if(!p.getType().equals(procParamsIt.next().getType()))
				return false;
		
		return true;
	}
	
	
	IProcedureCallExpression callExpression(List<IExpression> args);
	
	default IProcedureCallExpression callExpression(IExpression ... args) {
		return callExpression(Arrays.asList(args));
	}
}
