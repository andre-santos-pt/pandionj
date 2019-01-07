package model.program;

import java.util.Collection;

public interface IProgram extends IExecutable {
	Collection<IConstantDeclaration> getConstants();
	Collection<IProcedure> getProcedures();
	void addProcedure(IProcedure procedure);
	void setMainProcedure(IProcedure procedure);
	IProcedure getMainProcedure(); // return is contained in getProcedures
	Collection<IStruct> getStructs();
	Collection<IDataType> getDataTypes();
	IDataType getDataType(String id);
}
