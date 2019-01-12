package model.program;

import java.util.Arrays;
import java.util.List;

public interface IArrayVariableDeclaration extends IVariableDeclaration {

	int getArrayDimensions();

//	default IVariableRole getRole() {
//		return IVariableRole.NONE;
//	}
	
	IArrayLengthExpression lengthExpression(List<IExpression> indexes);
	default IArrayLengthExpression lengthExpression(IExpression ... indexes) {
		return lengthExpression(Arrays.asList(indexes));
	}
	
	IArrayElementExpression elementExpression(List<IExpression> indexes);
	default IArrayElementExpression elementExpression(IExpression ... indexes) {
		return elementExpression(Arrays.asList(indexes));
	}
	
	IArrayElementAssignment elementAssignment(IExpression expression, List<IExpression> indexes);
	default IArrayElementAssignment elementAssignment(IExpression expression, IExpression ... indexes) {
		return elementAssignment(expression, Arrays.asList(indexes));
	}
	
}
