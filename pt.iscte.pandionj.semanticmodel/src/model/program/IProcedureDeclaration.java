package model.program;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public interface IProcedureDeclaration extends IIdentifiableElement {

	List<IVariableDeclaration> getParameters();	

	IDataType getReturnType();

	IVariableDeclaration addParameter(String id, IDataType type);

	IProcedureCallExpression callExpression(List<IExpression> args);

	default IProcedureCallExpression callExpression(IExpression ... args) {
		return callExpression(Arrays.asList(args));
	}
	
	default boolean matchesSignature(String id, IDataType... paramTypes) {
		if(!getId().equals(id))
			return false;
		
		List<IVariableDeclaration> parameters = getParameters();
		if(parameters.size() != paramTypes.length)
			return false;
		
		int i = 0;
		for(IDataType t : paramTypes)
			if(!parameters.get(i++).getType().equals(t))
				return false;
		
		return true;
	}

	// compares id and types of parameters
	// excludes return
	default boolean hasSameSignature(IProcedureDeclaration procedure) {
		if(!getId().equals(procedure.getId()) || getParameters().size() != procedure.getParameters().size())
			return false;

		Iterator<IVariableDeclaration> procParamsIt = procedure.getParameters().iterator();
		for(IVariableDeclaration p : getParameters())
			if(!p.getType().equals(procParamsIt.next().getType()))
				return false;

		return true;
	}
	
	default boolean isEqualTo(IProcedureDeclaration procedure) {
		return hasSameSignature(procedure) && getReturnType().equals(procedure.getReturnType());
	}

}
