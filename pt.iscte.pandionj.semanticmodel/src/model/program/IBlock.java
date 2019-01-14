package model.program;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import model.machine.ICallStack;

public interface IBlock extends ISourceElement, IExecutable, IStatement, Iterable<IStatement> {
	IBlock getParent();
	
	default boolean isProcedure() {
		return getParent() == null;
	}
	
	List<IStatement> getStatements();
	
	void addStatement(IStatement statement);
	
	default boolean isEmpty() {
		return getStatements().isEmpty();
	}
	
	IBlock block();
	
	IVariableDeclaration variableDeclaration(String name, IDataType type, Set<IVariableDeclaration.Flag> flags);
	default IVariableDeclaration variableDeclaration(String name, IDataType type) {
		return variableDeclaration(name, type, ImmutableSet.of());
	}
	
	IArrayVariableDeclaration arrayDeclaration(String name, IDataType type, int dimensions, Set<IVariableDeclaration.Flag> flags);
	default IArrayVariableDeclaration arrayDeclaration(String name, IDataType type, int dimensions) {
		return arrayDeclaration(name, type, dimensions, ImmutableSet.of());
	}
	
	IVariableAssignment assignment(IVariableDeclaration var, IExpression exp);
	
	default ISelection selection(IExpression expression, IBlock block) {
		return selection(expression, block, null);
	}
	
	ISelection selection(IExpression expression, IBlock block, IBlock alternativeBlock);

	ILoop loop(IExpression guard);
	
	IReturn returnStatement(IExpression expression);
	
	IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args);
	
	default IProcedureCall procedureCall(IProcedure procedure, IExpression ... args) {
		return procedureCall(procedure, Arrays.asList(args));
	}
	
	@Override
	default void execute(ICallStack stack) throws ExecutionError {
		for(IStatement s : getStatements()) {
			stack.execute(s);
			if(s instanceof IReturn)
				; // TODO quit procedure
		}
	}
	
	@Override
	default Iterator<IStatement> iterator() {
		return getStatements().iterator();
	}
	
	default void accept(IVisitor visitor) {
		for(IStatement s : getStatements()) {
			if(s instanceof IVariableAssignment)
				visitor.visitVariableAssignment((IVariableAssignment) s);
			else if(s instanceof IArrayElementAssignment)
				visitor.visitArrayElementAssignment((IArrayElementAssignment) s);
			else if(s instanceof IBlock)
				((IBlock) s).accept(visitor);
		}
	}
	
	interface IVisitor {
		default void visitVariableAssignment(IVariableAssignment assignment) { }
		default void visitArrayElementAssignment(IVariableAssignment assignment) { }
	}
	
}
