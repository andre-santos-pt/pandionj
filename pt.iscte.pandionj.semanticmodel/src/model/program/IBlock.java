package model.program;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import model.machine.ICallStack;

public interface IBlock extends ISourceElement, IExecutable, IStatement, Iterable<IStatement> {
	IBlock getParent();
	List<IStatement> getStatements();
	
	default boolean isEmpty() {
		return getStatements().isEmpty();
	}
	
	IBlock createBlock();
	
	IVariableDeclaration createVariableDeclaration(String name, IDataType type);
	IVariableAssignment createAssignment(IVariableDeclaration var, IExpression exp);
	default ISelection createSelection(IExpression expression, IBlock block) {
		return createSelection(expression, block, null);
	}
	ISelection createSelection(IExpression expression, IBlock block, IBlock alternativeBlock);

	IReturn createReturn(IExpression expression);
	
	IProcedureCall createProcedureCall(IProcedure procedure, List<IExpression> args);
	default IProcedureCall createProcedureCall(IProcedure procedure, IExpression ... args) {
		return createProcedureCall(procedure, Arrays.asList(args));
	}
	
	@Override
	default void execute(ICallStack stack) {
		for(IStatement s : getStatements())
			stack.getTopFrame().execute(s);
	}
	
	@Override
	default Iterator<IStatement> iterator() {
		return getStatements().iterator();
	}
	
}
