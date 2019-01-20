package model.program;

import java.util.List;

interface IObjectType extends IStructType {

	List<IProcedure> getProcedures();
	
	default boolean isCompatibleWith(IAbstractDataType type) {
		for (IProcedure tp : type.getProcedures()) {
			boolean match = false;
			for(IProcedure p : getProcedures())
				if(p.hasSameSignature(tp)) {
					match = true;
					break;
				}
			if(!match)
				return false;
		}
		return true;
	}
}
