package model.program;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import model.machine.ICallStack;
import model.program.impl.Factory;

public interface IBlock extends ISourceElement, IExecutable, IStatement, Iterable<IStatement> {
	IFactory factory = new Factory();
	
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
	
	IArrayVariableDeclaration arrayDeclaration(String name, IArrayType type, Set<IVariableDeclaration.Flag> flags);
	default IArrayVariableDeclaration arrayDeclaration(String name, IArrayType type) {
		return arrayDeclaration(name, type, ImmutableSet.of());
	}
	
	IVariableAssignment assignment(IVariableDeclaration var, IExpression exp);
	
	default IVariableAssignment increment(IVariableDeclaration var) {
		assert var.getType() == IDataType.INT;
		return assignment(var, factory.binaryExpression(IOperator.ADD, var.expression(), factory.literal(1)));
	}
	
	default IVariableAssignment decrement(IVariableDeclaration var) {
		assert var.getType() == IDataType.INT;
		return assignment(var, factory.binaryExpression(IOperator.SUB, var.expression(), factory.literal(1)));
	}
	
	IArrayElementAssignment arrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, List<IExpression> indexes);
	default IArrayElementAssignment arrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, IExpression ... indexes) {
		return arrayElementAssignment(var, exp, Arrays.asList(indexes));
	}
	
	default ISelection selection(IExpression expression, IBlock block) {
		return selection(expression, block, null);
	}
	
	ISelection selection(IExpression expression, IBlock selectionBlock, IBlock alternativeBlock);

	ILoop loop(IExpression guard);
	
	IReturn returnStatement(IExpression expression);
	
	IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args);
	
	default IProcedureCall procedureCall(IProcedure procedure, IExpression ... args) {
		return procedureCall(procedure, Arrays.asList(args));
	}
	
	@Override
	default boolean execute(ICallStack stack) throws ExecutionError {
		for(IStatement s : getStatements()) {
			if(!stack.execute(s))
				return false;
//			if(s instanceof IReturn)
				; // TODO quit procedure
		}
		return true;
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
