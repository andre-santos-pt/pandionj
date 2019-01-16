package model.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface IProgram extends IExecutable, ISourceElement {
	Collection<IConstantDeclaration> getConstants(); // not there yet
	Collection<IProcedure> getProcedures();
	
	IProcedure createProcedure(String id, IDataType returnType);
	
	Collection<IStruct> getStructs(); // not there yet
	Collection<IDataType> getDataTypes();
	
	IDataType getDataType(String id);
	

	// TODO program validation
	@Override
	default List<ISemanticProblem> validateSematics() {
		List<ISemanticProblem> problems = new ArrayList<ISemanticProblem>();
		
		// TODO constants
		
		Collection<IProcedure> procedures = getProcedures();
		for (IProcedure p : procedures) {
			for(IProcedure p2 : procedures) {
				if(p2 != p && p2.hasSameSignature(p))
					problems.add(ISemanticProblem.create("procedures with the same signature", p, p2));
			}
			problems.addAll(p.validateSematics());
		}
		return problems;
	}
	
	// TODO test
	default IProcedure getProcedure(String id, IDataType ... paramTypes) {
		for(IProcedure p : getProcedures())
			if(p.getId().equals(id) && p.getNumberOfParameters() == paramTypes.length) {
				boolean match = true;
				int i = 0;
				for (IVariableDeclaration param : p.getParameters()) {
					if(!param.getType().equals(paramTypes[i++])) {
						match = false;
						break;
					}
				}
				if(match)
					return p;
			}
		return null;
	}
}
