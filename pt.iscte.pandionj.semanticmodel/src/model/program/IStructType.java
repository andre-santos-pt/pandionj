package model.program;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import model.program.IVariableDeclaration.Flag;

public interface IStructType extends IDataType, IProgramElement {
	
	List<IVariableDeclaration> getMemberVariables();
	
	IVariableDeclaration addMemberVariable(String id, IDataType type, Set<Flag> flags);
	default IVariableDeclaration addMemberVariable(String id, IDataType type) {
		return addMemberVariable(id, type, ImmutableSet.of());
	}
	
	@Override
	default Object getDefaultValue() {
		return null;
	}

	IStructAllocation allocationExpression();
}
