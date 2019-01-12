package model.program;

import java.util.Collection;

public interface IProgram extends IExecutable, ISourceElement {
	Collection<IConstantDeclaration> getConstants();
	Collection<IProcedure> getProcedures();
	IProcedure createProcedure(String name, IDataType returnType);

//	void setMainProcedure(IProcedure procedure);
//	IProcedure getMainProcedure(); // return is contained in getProcedures
	Collection<IStruct> getStructs();
	Collection<IDataType> getDataTypes();
	IDataType getDataType(String id);
	

}
