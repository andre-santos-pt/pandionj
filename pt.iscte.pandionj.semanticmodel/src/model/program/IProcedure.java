package model.program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public interface IProcedure extends IBlock, IIdentifiableElement {
	IVariableDeclaration addParameter(String id, IDataType type, Set<IVariableDeclaration.Flag> flags);
	default IVariableDeclaration addParameter(String id, IDataType type) {
		return addParameter(id, type, ImmutableSet.of());
	}
	
	Iterable<IVariableDeclaration> getParameters();	
	int getNumberOfParameters();
	Iterable<IVariableDeclaration> getVariables(boolean includingParams);
	
	IVariableDeclaration getVariable(String id);
	IDataType getReturnType();
	
	
	default boolean isBuiltIn() {
		return false;
	}
	
	
	default boolean isFunction() {
		return false;
	}
	
	default boolean isRecursive() {
		return false;
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
	
	default List<ISemanticProblem> validateSematics() {
		List<ISemanticProblem> problems = new ArrayList<ISemanticProblem>();
		SemanticChecks.checkVariableNames(this, problems);
		SemanticChecks.checkReturn(this, problems);
		// check return paths
		// variable not initialized
		return problems;
	}
	
	IProcedureCallExpression callExpression(List<IExpression> args);
	default IProcedureCallExpression callExpression(IExpression ... args) {
		return callExpression(Arrays.asList(args));
	}
}
