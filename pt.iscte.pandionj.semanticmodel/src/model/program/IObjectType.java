package model.program;

import java.util.List;

interface IObjectType extends IStructType {

	List<IProcedure> getProcedures();
	
	IAbstractDataType getRealizations();
	
	default boolean isCompatibleWith(IAbstractDataType type) {
		for (IProcedureDeclaration tp : type.getProcedures()) {
			boolean match = false;
			for(IProcedure p : getProcedures())
				if(p.isEqualTo(tp)) {
					match = true;
					break;
				}
			if(!match)
				return false;
		}
		return true;
	}
}
