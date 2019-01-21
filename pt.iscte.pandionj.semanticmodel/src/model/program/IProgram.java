package model.program;

import java.util.Collection;

/**
 * Mutable
 */
public interface IProgram extends IProgramElement {
	Collection<IConstantDeclaration> getConstants();
	Collection<IStructType> getStructs();
	Collection<IProcedure> getProcedures();
	Collection<IDataType> getDataTypes();
	
	IDataType getDataType(String id);

	IConstantDeclaration addConstant(String id, IDataType type, ILiteral value);
	IStructType addStruct(String id);
	IProcedure addProcedure(String id, IDataType returnType);
	

//	// TODO program validation
//	default List<ISemanticProblem> validateSematics() {
//		List<ISemanticProblem> problems = new ArrayList<ISemanticProblem>();
//		
//		// TODO constants
//		
//		Collection<IProcedure> procedures = getProcedures();
//		for (IProcedure p : procedures) {
//			for(IProcedure p2 : procedures) {
//				if(p2 != p && p2.hasSameSignature(p))
//					problems.add(ISemanticProblem.create("procedures with the same signature", p, p2));
//			}
//			problems.addAll(p.validateSematics());
//		}
//		return problems;
//	}
	
	IConstantDeclaration getConstant(String id);
//	default IConstantDeclaration getConstant(String id) {
//		for (IConstantDeclaration c : getConstants()) {
//			if(c.getId().equals(id))
//				return c;
//		}
//		return null;
//	}
	
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
	
	default void accept(IVisitor visitor) {
		getConstants().forEach(c -> visitor.visit(c));
		getStructs().forEach(s -> visitor.visit(s));
		getProcedures().forEach(p -> {
			if(visitor.visit(p))
				p.getBody().accept(visitor);
		});
	}	
	
	
	interface IVisitor extends IBlock.IVisitor {
		default boolean visit(IConstantDeclaration constant) { return true; }
		default boolean visit(IStructType constant) { return true; }
		default boolean visit(IProcedure constant) { return true; }
	}
	
}
