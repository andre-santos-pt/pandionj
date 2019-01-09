package model.program;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import model.machine.ICallStack;

public interface IProcedure extends IIdentifiableElement, IExecutable, IBlock {
	IVariableDeclaration addParameter(String name, IDataType type);
	Iterable<IVariableDeclaration> getParameters();	
	int getNumberOfParameters();
	Iterable<IVariableDeclaration> getVariables(boolean includingParams);
	
	
	IBlock getBody();
//	void setBody(IBlock body);
	
//	default boolean isDeclaration() {
//		return getBody() == null;
//	}
	
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
	default void execute(ICallStack stack) {
		for(IStatement s : this)
			stack.getTopFrame().execute(s);
//		getBody().execute(stack);
//		System.out.println(toString() + " -> " + stack.getReturn());
	}
	
	
//	IVariableDeclaration declareVariable(String name, IDataType type);
	
	IProcedureCall call(List<IExpression> args);
	default IProcedureCall call(IExpression ... args) {
		return call(Arrays.asList(args));
	}
}
