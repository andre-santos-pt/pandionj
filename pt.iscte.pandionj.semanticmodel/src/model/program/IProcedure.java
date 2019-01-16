package model.program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import model.machine.ICallStack;

public interface IProcedure extends IIdentifiableElement, IExecutable, IBlock {
	IVariableDeclaration addParameter(String name, IDataType type, Set<IVariableDeclaration.Flag> flags);
	default IVariableDeclaration addParameter(String name, IDataType type) {
		return addParameter(name, type, ImmutableSet.of());
	}
	
	Iterable<IVariableDeclaration> getParameters();	
	int getNumberOfParameters();
	Iterable<IVariableDeclaration> getVariables(boolean includingParams);
	
	IVariableDeclaration getVariable(String id);
	
	IBlock getBody();
	
	default boolean isDeclaration() {
		return getBody() == null;
	}
	
	default boolean isBuiltIn() {
		return false;
	}
	
	IDataType getReturnType();
	
	boolean isFunction();
	
	boolean isRecursive();
	
	// compares id and types of parameters
	// excludes return
	
	default boolean hasSameSignature(IProcedure procedure) {
		if(!getId().equals(procedure.getId()) || getNumberOfParameters() != procedure.getNumberOfParameters())
//			|| !getReturnType().equals(procedure.getReturnType()))
			return false;
		
		Iterator<IVariableDeclaration> procParamsIt = procedure.getParameters().iterator();
		for(IVariableDeclaration p : getParameters())
			if(!p.getType().equals(procParamsIt.next().getType()))
				return false;
		
		return true;
	}
	
	@Override
	default List<ISemanticProblem> validateSematics() {
		List<ISemanticProblem> problems = new ArrayList<ISemanticProblem>();
		Util.checkVariableNames(this, problems);
		Util.checkReturn(this, problems);
		// check return paths
		// variable not initialized
		return problems;
	}
	
	class Util {
		private static void checkVariableNames(IProcedure procedure, List<ISemanticProblem> problems) {
			Map<String, IVariableDeclaration> ids = new HashMap<>();
			for(IVariableDeclaration v : procedure.getVariables(true))
				if(ids.containsKey(v.getId()))
					problems.add(ISemanticProblem.create("duplicate variable names", v, ids.get(v.getId())));
				else
					ids.put(v.getId(), v);
			
		}
		
		private static void checkReturn(IProcedure procedure, List<ISemanticProblem> problems) {
			IDataType returnType = procedure.getReturnType();
			procedure.accept(new IVisitor() {
				public void visitReturn(IReturn returnStatement) {
					IDataType t = returnStatement.getReturnValueType();
					if(!t.equals(returnType))
						problems.add(ISemanticProblem.create("return not compatible with procedure result: " + t + " " + returnType, returnStatement, procedure));
				}
			});
		}
	}
	
	@Override
	default boolean execute(ICallStack stack) throws ExecutionError {
		return stack.execute(getBody());
	}
	
	
	IProcedureCallExpression callExpression(List<IExpression> args);
	default IProcedureCallExpression callExpression(IExpression ... args) {
		return callExpression(Arrays.asList(args));
	}
}
