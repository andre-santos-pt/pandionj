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

	List<IInstruction> getInstructionSequence();

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

	IBreak addBreakStatement();

	IContinue addContinueStatement();
	
	IProcedureCall addProcedureCall(IProcedure procedure, List<IExpression> args);
	default IProcedureCall addProcedureCall(IProcedure procedure, IExpression ... args) {
		return addProcedureCall(procedure, Arrays.asList(args));
	}


	default void accept(IVisitor visitor) {
		for(IInstruction s : getInstructionSequence()) {
			
			if(s instanceof IReturn) {
				IReturn ret = (IReturn) s;
				if(visitor.visit(ret) && !ret.getReturnValueType().isVoid())
					visitor.visit(ret.getExpression());
			}
			else if(s instanceof IArrayElementAssignment) {
				IArrayElementAssignment ass = (IArrayElementAssignment) s;
				if(visitor.visit(ass))
					visitor.visit(ass.getExpression());
			}
			else if(s instanceof IVariableAssignment) {
				IVariableAssignment ass = (IVariableAssignment) s;
				if(visitor.visit(ass))
					visitor.visit(ass.getExpression());
			}
			else if(s instanceof IStructMemberAssignment) {
				IStructMemberAssignment ass = (IStructMemberAssignment) s;
				if(visitor.visit(ass))
					visitor.visit(ass.getExpression());
			}
			else if(s instanceof IProcedureCall) {
				IProcedureCall call = (IProcedureCall) s;
				if(visitor.visit(call))
					call.getArguments().forEach(a -> visitor.visit(a));
			}
			else if(s instanceof IBreak) {
				visitor.visit((IBreak) s);
			}
			else if(s instanceof IContinue) {
				visitor.visit((IContinue) s);				
			}
			else if(s instanceof ISelection) {
				ISelection sel = (ISelection) s;
				if(visitor.visit(sel)) {
					visitor.visit(sel.getGuard());
					sel.accept(visitor);
					if(sel instanceof ISelectionWithAlternative)
						((ISelectionWithAlternative) sel).getAlternativeBlock().accept(visitor);
				}
			}
			else if(s instanceof ILoop) {
				ILoop loop = (ILoop) s;
				if(visitor.visit(loop)) {
					visitor.visit(loop.getGuard());
					loop.accept(visitor);
				}
			}
			else if(s instanceof IBlock) { // only single blocks
				IBlock b = (IBlock) s;
				if(visitor.visit(b))
					b.accept(visitor);
			}
		}
	}

	interface IVisitor extends IExpression.IVisitor {
		default void	visit(IVariableDeclaration variable)		{ }
		
		// IStatement
		default boolean visit(IReturn returnStatement) 				{ return true; }
		default boolean visit(IArrayElementAssignment assignment) 	{ return true; }
		default boolean visit(IVariableAssignment assignment) 		{ return true; }
		default boolean visit(IStructMemberAssignment assignment) 	{ return true; }
		default boolean visit(IProcedureCall call) 					{ return true; }
		default void 	visit(IBreak breakStatement) 				{ }
		default void 	visit(IContinue continueStatement) 			{ }
		
		// IControlStructure
		default boolean visit(ISelection selection) 				{ return true; } // also ISelectionWithAlternative
		default boolean visit(ILoop loop) 							{ return true; }
		
		// other
		default boolean visit(IBlock block) 						{ return true; }
		default boolean visit(IExpression expression) 				{ return true;  }
	}

}
