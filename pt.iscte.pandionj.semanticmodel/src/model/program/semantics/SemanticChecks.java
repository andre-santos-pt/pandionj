package model.program.semantics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.program.IBlock;
import model.program.IDataType;
import model.program.IProcedure;
import model.program.IReturn;
import model.program.IVariableDeclaration;
import model.program.IBlock.IVisitor;

class SemanticChecks {
	static void checkVariableNames(IProcedure procedure, List<ISemanticProblem> problems) {
		Map<String, IVariableDeclaration> ids = new HashMap<>();
		for(IVariableDeclaration v : procedure.getVariables(true))
			if(ids.containsKey(v.getId()))
				problems.add(ISemanticProblem.create("duplicate variable names", v, ids.get(v.getId())));
			else
				ids.put(v.getId(), v);
		
	}
	
	static void checkReturn(IProcedure procedure, List<ISemanticProblem> problems) {
		IDataType returnType = procedure.getReturnType();
		procedure.getBody().accept(new IVisitor() {
			public boolean visit(IReturn returnStatement) {
				IDataType t = returnStatement.getReturnValueType();
				if(!t.equals(returnType))
					problems.add(ISemanticProblem.create("return not compatible with procedure result: " + t + " " + returnType, returnStatement, procedure));
				return true;
			}
		});
	}
}