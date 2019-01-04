package model.program;

import com.google.common.collect.ImmutableCollection;

public interface IAbstractDataType extends IDataType {
	ImmutableCollection<IProcedure> getProcedures();
}
