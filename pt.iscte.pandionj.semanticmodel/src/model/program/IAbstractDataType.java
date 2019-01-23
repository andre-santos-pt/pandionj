package model.program;

import java.util.Collection;

interface IAbstractDataType extends IDataType {
	Collection<IProcedureDeclaration> getProcedures();
}
