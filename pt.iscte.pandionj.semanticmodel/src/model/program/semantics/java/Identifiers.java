package model.program.semantics.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.program.IConstantDeclaration;
import model.program.IExpression;
import model.program.IIdentifiableElement;
import model.program.IProcedure;
import model.program.IProgramElement;
import model.program.IStructType;
import model.program.IVariableDeclaration;
import model.program.semantics.IRule;
import model.program.semantics.ISemanticProblem;

public class Identifiers extends IRule {

	
	Map<String, IConstantDeclaration> constantMap = new HashMap<String, IConstantDeclaration>();
	List<IStructType> structs = new ArrayList<IStructType>(); // TODO
	List<IProcedure> procedures = new ArrayList<IProcedure>();
	
	@Override
	public boolean visit(IConstantDeclaration constant) {
		if(!IIdentifiableElement.isValidIdentifier(constant.getId()))
			addProblem(ISemanticProblem.create("invalid identifier", constant));
		
		if(constantMap.containsKey(constant.getId()))
			addProblem(ISemanticProblem.create("duplicate constant name", constant, constantMap.get(constant.getId())));
		return true;
	}
	
	@Override
	public boolean visit(IStructType struct) {
		// TODO Auto-generated method stub
		return super.visit(struct);
	}
	
	@Override
	public boolean visit(IProcedure procedure) {
		procedures.forEach(p -> { 
			if(p.hasSameSignature(procedure))
				addProblem(ISemanticProblem.create("duplicate procedure signature", p, procedure));
		});
		Map<String, IVariableDeclaration> vars = new HashMap<String, IVariableDeclaration>();
		procedure.getVariables(true).forEach(v -> {
			if(vars.containsKey(v.getId()))
				addProblem(ISemanticProblem.create("duplicate local variable name", vars.get(v.getId()), v));
		});
		
		return super.visit(procedure);
	}
}
