package model.program;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Mutable
 */
public interface IBlock extends IInstruction {
	IProgramElement getParent();

	List<IProgramElement> getInstructionSequence();

	boolean isEmpty();

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
		IFactory factory = IFactory.INSTANCE;
		return addAssignment(var, factory.binaryExpression(IOperator.ADD, var.expression(), factory.literal(1)));
	}

	default IVariableAssignment addDecrement(IVariableDeclaration var) {
		assert var.getType() == IDataType.INT;
		IFactory factory = IFactory.INSTANCE;
		return addAssignment(var, factory.binaryExpression(IOperator.SUB, var.expression(), factory.literal(1)));
	}

	IArrayElementAssignment addArrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, List<IExpression> indexes);
	default IArrayElementAssignment arrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, IExpression ... indexes) {
		return addArrayElementAssignment(var, exp, Arrays.asList(indexes));
	}

	IStructMemberAssignment addStructMemberAssignment(IVariableDeclaration var, String memberId, IExpression exp);


	ISelection addSelection(IExpression guard);
	
	ISelectionWithAlternative addSelectionWithAlternative(IExpression guard);

	ILoop addLoop(IExpression guard);

	IReturn addReturnStatement(IExpression expression);

	IProcedureCall addProcedureCall(IProcedure procedure, List<IExpression> args);
	default IProcedureCall addProcedureCall(IProcedure procedure, IExpression ... args) {
		return addProcedureCall(procedure, Arrays.asList(args));
	}


	default void accept(IVisitor visitor) {
		for(IProgramElement s : getInstructionSequence()) {
			if(s instanceof IReturn) {
				IReturn ret = (IReturn) s;
				if(visitor.visitReturn(ret) && !ret.getReturnValueType().isVoid())
					visitor.visitExpression(ret.getExpression());
			}
			else if(s instanceof IArrayElementAssignment) {
				IArrayElementAssignment ass = (IArrayElementAssignment) s;
				if(visitor.visitArrayElementAssignment(ass))
					visitor.visitExpression(ass.getExpression());
			}
			else if(s instanceof IVariableAssignment) {
				IVariableAssignment ass = (IVariableAssignment) s;
				if(visitor.visitVariableAssignment(ass))
					visitor.visitExpression(ass.getExpression());
			}
			else if(s instanceof IProcedureCall) {
				IProcedureCall call = (IProcedureCall) s;
				if(visitor.visitProcedureCall(call.getProcedure(), call.getArguments()))
					call.getArguments().forEach(a -> visitor.visitExpression(a));
			}
			else if(s instanceof IProcedureCallExpression) {
				IProcedureCallExpression call = (IProcedureCallExpression) s;
				if(visitor.visitProcedureCall(call.getProcedure(), call.getArguments()))
					call.getArguments().forEach(a -> visitor.visitExpression(a));
			}
			else if(s instanceof ISelection) {
				ISelection sel = (ISelection) s;
				if(visitor.visitSelection(sel)) {
					sel.accept(visitor);
					if(sel instanceof ISelectionWithAlternative)
						((ISelectionWithAlternative) sel).getAlternativeBlock().accept(visitor);
				}
			}
			else if(s instanceof ILoop) {
				ILoop loop = (ILoop) s;
				if(visitor.visitLoop(loop))
					loop.accept(visitor);
			}
			else if(s instanceof IBlock) { // only single blocks
				IBlock b = (IBlock) s;
				if(visitor.visitBlock(b))
					b.accept(visitor);
			}
		}
	}

	interface IVisitor {
		default boolean visitReturn(IReturn returnStatement) { return false; }
		default boolean visitArrayElementAssignment(IArrayElementAssignment assignment) { return false;  }
		default boolean visitVariableAssignment(IVariableAssignment assignment) {  return false; }
		default boolean visitProcedureCall(IProcedure procedure, List<IExpression> args) { return false;  }
		default boolean visitSelection(ISelection block) { return false; }
		default boolean visitLoop(ILoop lool) { return false; }
		default boolean visitBlock(IBlock block) { return false; }
		default void visitExpression(IExpression expression) {  }
	}

}
