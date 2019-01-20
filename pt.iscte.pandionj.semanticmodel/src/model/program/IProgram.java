package model.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface IProgram {
	Collection<IConstantDeclaration> getConstants();
	Collection<IProcedure> getProcedures();
	
	Collection<IStructType> getStructs();
	Collection<IDataType> getDataTypes();
	
	IDataType getDataType(String id);

	IConstantDeclaration addConstant(String id, IDataType type, ILiteral value);
	IStructType addStruct(String id);
	IProcedure addProcedure(String id, IDataType returnType);
	

	// TODO program validation
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
	
	IConstantDeclaration getConstant(String id);
	
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
