package model.program;

import java.util.List;

public interface IExpression extends IProgramElement {
	IDataType getType();
	
	// TODO concretize expression
	//String concretize();
//	ISourceElement getParent();
	
	default OperationType getOperationType() {
		return OperationType.OTHER;
	}
	
	enum OperationType {
		ARITHMETIC, RELATIONAL, LOGICAL, CALL, OTHER;
	}
	
	boolean isDecomposable();
	
	default int getNumberOfParts() {
		return decompose().size();
	}
	
	List<IExpression> decompose();

}
