package model.program;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import model.program.IVariableDeclaration.Flag;

// TODO
public interface IStructType extends IDataType, ISourceElement {
	
	List<IVariableDeclaration> getMemberVariables();
	
	IVariableDeclaration addMemberVariable(String id, IDataType type, Set<Flag> flags);
	default IVariableDeclaration addMemberVariable(String id, IDataType type) {
		return addMemberVariable(id, type, ImmutableSet.of());
	}
	
	IStructMemberExpression memberExpression(String id);
	
	@Override
	default Object getDefaultValue() {
		return null;
	}
//	ImmutableList<IProcedure> getProcedures();
//	default boolean isCompatibleWith(IAbstractDataType type) {
//		for (IProcedure tp : type.getProcedures()) {
//			boolean match = false;
//			for(IProcedure p : getProcedures())
//				if(p.hasSameSignature(tp)) {
//					match = true;
//					break;
//				}
//			if(!match)
//				return false;
//		}
//		return true;
//	}
}
