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
	
	IBlock block();
	
	IVariableDeclaration variableDeclaration(String name, IDataType type);
	
	IArrayVariableDeclaration arrayDeclaration(String name, IDataType type, int dimensions);
	
	IVariableAssignment assignment(IVariableDeclaration var, IExpression exp);
	
	default ISelection selection(IExpression expression, IBlock block) {
		return selection(expression, block, null);
	}
	
	ISelection selection(IExpression expression, IBlock block, IBlock alternativeBlock);

	IReturn returnStatement(IExpression expression);
	
	IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args);
	
	default IProcedureCall procedureCall(IProcedure procedure, IExpression ... args) {
		return procedureCall(procedure, Arrays.asList(args));
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
