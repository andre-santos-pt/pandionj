package model.program;

import com.google.common.collect.ImmutableList;

public interface IStruct extends IDataType, ISourceElement {
	ImmutableList<IVariableDeclaration> getVariables();
	ImmutableList<IProcedure> getProcedures();
	
	default boolean isCompatibleWith(IAbstractDataType type) {
		for (IProcedure tp : type.getProcedures()) {
			boolean match = false;
			for(IProcedure p : getProcedures())
				if(p.isSameAs(tp)) {
					match = true;
					break;
				}
			if(!match)
				return false;
		}
		return true;
	}
}
