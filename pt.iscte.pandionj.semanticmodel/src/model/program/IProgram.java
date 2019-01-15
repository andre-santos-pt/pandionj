package model.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface IProgram extends IExecutable, ISourceElement {
	Collection<IConstantDeclaration> getConstants();
	Collection<IProcedure> getProcedures();
	IProcedure createProcedure(String id, IDataType returnType);
	IProcedure getProcedure(String id); // TODO signature
	Collection<IStruct> getStructs();
	Collection<IDataType> getDataTypes();
	IDataType getDataType(String id);
	

	// TODO
	@Override
	default List<IProblem> validate() {
		List<IProblem> problems = new ArrayList<IProblem>();
		for (IProcedure p : getProcedures()) {
			problems.addAll(p.validate());
		}
		return problems;
	}
}
