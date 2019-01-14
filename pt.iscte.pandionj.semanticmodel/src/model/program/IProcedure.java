package model.program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
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
	
	IDataType getReturnType();
	
	boolean isFunction();
	boolean isRecursive();
	
	default boolean hasSameSignature(IProcedure procedure) {
		if(!getIdentifier().equals(procedure.getIdentifier()) || 
			getNumberOfParameters() != procedure.getNumberOfParameters() ||
			!getReturnType().equals(procedure.getReturnType()))
			return false;
		
		Iterator<IVariableDeclaration> procParamsIt = procedure.getParameters().iterator();
		for(IVariableDeclaration p : getParameters())
			if(!p.equals(procParamsIt.next()))
				return false;
		
		return true;
	}
	
	
	@Override
	default void execute(ICallStack stack) throws ExecutionError {
		stack.execute(getBody());
//		stack.getTopFrame().terminateFrame();
	}
	
	
	IProcedureCall callExpression(List<IExpression> args);
	default IProcedureCall call(IExpression ... args) {
		return callExpression(Arrays.asList(args));
	}
	
	@Override
	default List<IProblem> validate() {
		List<IProblem> problems = new ArrayList<IProblem>();
		Map<String, IVariableDeclaration> ids = new HashMap<>();
		for(IVariableDeclaration v : getVariables(true))
			if(ids.containsKey(v.getIdentifier())) {
				problems.add(new IProblem() {
					
					@Override
					public List<ISourceElement> getSourceElements() {
						return ImmutableList.of(v, ids.get(v.getIdentifier()));
					}
					
					@Override
					public String getMessage() {
						return "Duplicate variable names";
					}
					
					@Override
					public String toString() {
						return getMessage() + ": " + getSourceElements();
					}
				});
			}
			else
				ids.put(v.getIdentifier(), v);
		
		return problems;
	}
	
	default void accept(IVisitor visitor) {
		for(IStatement s : getStatements()) {
			if(s instanceof IVariableAssignment)
				visitor.visitVariableAssignment((IVariableAssignment) s);
			else if(s instanceof IArrayElementAssignment)
				visitor.visitArrayElementAssignment((IArrayElementAssignment) s);
		}
	}
	
	interface IVisitor {
		default void visitVariableAssignment(IVariableAssignment assignment) { }
		default void visitArrayElementAssignment(IVariableAssignment assignment) { }
	}
}
