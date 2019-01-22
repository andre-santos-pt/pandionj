package model.program;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

/**
 * Mutable
 */
public interface IModule extends IIdentifiableElement {
	default Collection<IModule> getImports() {
		// TODO imports
		return ImmutableList.of();
	}
	Collection<IConstantDeclaration> getConstants();
	Collection<IStructType> getStructs();
	Collection<IProcedure> getProcedures();
	
//	Collection<IDataType> getDataTypes();

//	IDataType getDataType(String id);

	IConstantDeclaration addConstant(String id, IDataType type, ILiteral value);
	IStructType addStruct(String id);
	IProcedure addProcedure(String id, IDataType returnType);



//	IConstantDeclaration getConstant(String id);
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
		getConstants().forEach(c -> {
			if(visitor.visit(c))
				visitor.visit(c.getValue());
		});
		
		getStructs().forEach(s -> {
			if(visitor.visit(s))
				s.getMemberVariables().forEach(v -> {
					visitor.visit(v);
				});
		});
		
		getProcedures().forEach(p -> {
			if(visitor.visit(p))
				p.getBody().accept(visitor);
		});
	}	


	interface IVisitor extends IBlock.IVisitor {
		default boolean visit(IConstantDeclaration constant) 	{ return true; }
		default boolean visit(IStructType struct) 				{ return true; }
		default boolean visit(IProcedure procedure) 			{ return true; }
	}



}
