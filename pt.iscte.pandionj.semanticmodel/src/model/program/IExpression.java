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
}
