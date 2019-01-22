package model.program;

import java.util.List;

import model.program.IOperator.OperationType;

/**
 * Immutable
 *
 */
public interface IExpression extends IProgramElement {
	IDataType getType();

	// TODO concretize expression
	//String concretize();
	//	ISourceElement getParent();


	boolean isDecomposable();

	default int getNumberOfParts() {
		return decompose().size();
	}

	List<IExpression> decompose();

	default OperationType getOperationType() {
		return OperationType.OTHER;
	}

	default void accept(IVisitor visitor) {
		for (IExpression part : decompose()) {
			if(part instanceof IArrayAllocation)
				if(visitor.visit((IArrayAllocation) part))
					part.accept(visitor);
			// TODO
		}
	}

	interface IVisitor {
		default boolean visit(IArrayAllocation exp) { return true; }
		default boolean visit(IArrayLengthExpression exp) { return true; }
		default boolean visit(IBinaryExpression exp) { return true; }
		default void 	visit(IConstantExpression exp) { }
		default void 	visit(ILiteral exp) {  }
		default boolean visit(IProcedureCallExpression exp) { return true; }
		default boolean visit(IStructAllocation exp) { return true; }
		default void 	visit(IStructMemberExpression exp) {  }
		default boolean visit(IUnaryExpression exp) { return true; }
		default void 	visit(IVariableExpression exp) {  }
		default boolean visit(IArrayElementExpression exp) { return true; }
	}
}
