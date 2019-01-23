package model.program.semantics.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.program.IConstantDeclaration;
import model.program.IIdentifiableElement;
import model.program.IProcedure;
import model.program.IStructType;
import model.program.IVariableDeclaration;
import model.program.semantics.ISemanticProblem;
import model.program.semantics.Rule;

public class Identifiers extends Rule {

	
	Map<String, IConstantDeclaration> constantMap = new HashMap<String, IConstantDeclaration>();
	List<IStructType> structs = new ArrayList<IStructType>(); // TODO
	List<IProcedure> procedures = new ArrayList<IProcedure>();
	
	@Override
	public boolean visit(IConstantDeclaration constant) {
		System.out.println(constant);
		if(!IIdentifiableElement.isValidIdentifier(constant.getId()))
			addProblem(ISemanticProblem.create("invalid identifier", constant));
		
		if(constantMap.containsKey(constant.getId()))
			addProblem(ISemanticProblem.create("duplicate constant name", constant, constantMap.get(constant.getId())));
		else
			constantMap.put(constant.getId(), constant);
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
			if(p != procedure && p.hasSameSignature(procedure))
				addProblem(ISemanticProblem.create("duplicate procedure signature", p, procedure));
		});
		
		Map<String, IVariableDeclaration> vars = new HashMap<String, IVariableDeclaration>();
		procedure.getVariables().forEach(v -> {
			if(vars.containsKey(v.getId()))
				addProblem(ISemanticProblem.create("duplicate local variable name", vars.get(v.getId()), v));
			else
				vars.put(v.getId(), v);
		});
		
		return super.visit(procedure);
	}
}
