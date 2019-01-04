package model.program;

import java.util.Collection;

public interface IProgram extends IExecutable {
	Collection<IConstantDeclaration> getConstants();
	Collection<IProcedure> getProcedures();
	IProcedure getMainProcedure(); // return is contained in getProcedures
	Collection<IStruct> getStructs();
}
