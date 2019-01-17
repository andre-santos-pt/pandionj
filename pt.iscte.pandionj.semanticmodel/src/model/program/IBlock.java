package model.program;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import impl.program.Factory;
import model.machine.ICallStack;

public interface IBlock extends IStatement, Iterable<IStatement> {
	IFactory factory = new Factory();
	
	IBlock getParent();
	
	List<IStatement> getStatements();
	
	default boolean isEmpty() {
		return getStatements().isEmpty();
	}
	
	@Override
	default boolean isControl() {
		return true;
	}
	
	
	IVariableDeclaration addVariableDeclaration(String name, IDataType type, Set<IVariableDeclaration.Flag> flags);
	default IVariableDeclaration addVariableDeclaration(String name, IDataType type) {
		return addVariableDeclaration(name, type, ImmutableSet.of());
	}
	
	IArrayVariableDeclaration addArrayDeclaration(String name, IArrayType type, Set<IVariableDeclaration.Flag> flags);
	default IArrayVariableDeclaration addArrayDeclaration(String name, IArrayType type) {
		return addArrayDeclaration(name, type, ImmutableSet.of());
	}
	
	IBlock addBlock();

	IVariableAssignment addAssignment(IVariableDeclaration var, IExpression exp);
	
	default IVariableAssignment addIncrement(IVariableDeclaration var) {
		assert var.getType() == IDataType.INT;
		return addAssignment(var, factory.binaryExpression(IOperator.ADD, var.expression(), factory.literal(1)));
	}
	
	default IVariableAssignment addDecrement(IVariableDeclaration var) {
		assert var.getType() == IDataType.INT;
		return addAssignment(var, factory.binaryExpression(IOperator.SUB, var.expression(), factory.literal(1)));
	}
	
	IArrayElementAssignment addArrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, List<IExpression> indexes);
	default IArrayElementAssignment arrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, IExpression ... indexes) {
		return addArrayElementAssignment(var, exp, Arrays.asList(indexes));
	}
	
	IStructMemberAssignment addStructMemberAssignment(IVariableDeclaration var, String memberId, IExpression exp);
	
	
	ISelection addSelection(IExpression guard);

	ILoop addLoop(IExpression guard);
	
	IReturn addReturnStatement(IExpression expression);
	
	IProcedureCall addProcedureCall(IProcedure procedure, List<IExpression> args);
	default IProcedureCall addProcedureCall(IProcedure procedure, IExpression ... args) {
		return addProcedureCall(procedure, Arrays.asList(args));
	}
	
	@Override
	default boolean execute(ICallStack stack) throws ExecutionError {
		for(IStatement s : getStatements()) {
			if(!stack.execute(s))
				return false;
		}
		return true;
	}
	
	@Override
	default Iterator<IStatement> iterator() {
		return getStatements().iterator();
	}
	
	
	default void accept(IVisitor visitor) {
		for(IStatement s : getStatements()) {
			if(s instanceof IReturn)
				visitor.visitReturn((IReturn) s);
			
			else if(s instanceof IArrayElementAssignment)
				visitor.visitArrayElementAssignment((IArrayElementAssignment) s);
			
			else if(s instanceof IVariableAssignment)
				visitor.visitVariableAssignment((IVariableAssignment) s);
			
			else if(s instanceof IProcedureCall) {
				IProcedureCall call = (IProcedureCall) s;
				visitor.visitProcedureCall(call.getProcedure(), call.getArguments());
			}
			else if(s instanceof IProcedureCallExpression) {
				IProcedureCallExpression call = (IProcedureCallExpression) s;
				visitor.visitProcedureCall(call.getProcedure(), call.getArguments());
			}
			else if(s instanceof ISelection) {
				ISelection sel = (ISelection) s;
				visitor.visitSelection(sel);
				sel.getSelectionBlock().accept(visitor);
				IBlock alternativeBlock = sel.getAlternativeBlock();
				if(alternativeBlock != null) 
					alternativeBlock.accept(visitor);
			}
			else if(s instanceof ILoop) {
				ILoop loop = (ILoop) s;
				visitor.visitLoop(loop);
				loop.accept(visitor);
			}
			else if(s instanceof IBlock) { // only single blocks
				visitor.visitBlock((IBlock) s);
				((IBlock) s).accept(visitor);
			}
		}
	}
	
	interface IVisitor {
		default void visitReturn(IReturn returnStatement) { }
		default void visitArrayElementAssignment(IArrayElementAssignment assignment) { }
		default void visitVariableAssignment(IVariableAssignment assignment) { }
		default void visitProcedureCall(IProcedure procedure, List<IExpression> args) { }
		default void visitSelection(ISelection block) { }
		default void visitLoop(ILoop lool) { }
		default void visitBlock(IBlock block) { }
	}
	
}
