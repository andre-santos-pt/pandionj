package model.program.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import model.program.IConstantDeclaration;
import model.program.IDataType;
import model.program.IProblem;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IStruct;

class Program extends SourceElement implements IProgram {
	private List<IProcedure> procedures;
	private IProcedure mainProcedure;
	private Map<String, IDataType> types;
	
	public Program() {
		procedures = new ArrayList<IProcedure>();
		types = new LinkedHashMap<>();
		for(IDataType t : IDataType.DEFAULTS)
			types.put(t.getIdentifier(), t);
	}
	
	@Override
	public Collection<IConstantDeclaration> getConstants() {
		return ImmutableList.of();
	}

	@Override
	public Collection<IProcedure> getProcedures() {
		return procedures;
	}

	@Override
	public IProcedure createProcedure(String name, IDataType returnType) {
		IProcedure proc = new Procedure(name, returnType);
		procedures.add(proc);
		return proc;
	}
	
//	public void addProcedure(IProcedure procedure) {
//		assert !procedureExists(procedure);
//		procedures.add(procedure);
//	}
//	
//	private boolean procedureExists(IProcedure procedure) {
//		for(IProcedure p : procedures)
//			if(p.hasSameSignature(procedure))
//				return true;
//		
//		return false;
//	}
	
	@Override
	public IProcedure getMainProcedure() {
		return mainProcedure;
	}
	
	public void setMainProcedure(IProcedure mainProcedure) {
		assert mainProcedure != null : "cannot be null";
		assert procedures.contains(mainProcedure) : "main procedure must be added to the program";
		
		this.mainProcedure = mainProcedure;
	}

	@Override
	public Collection<IStruct> getStructs() {
		return ImmutableList.of();
	}

	@Override
	public Collection<IDataType> getDataTypes() {
		return Collections.unmodifiableCollection(types.values());
	}
	
	@Override
	public IDataType getDataType(String id) {
		assert types.containsKey(id);
		return types.get(id);
	}
	
	@Override
	public List<IProblem> validate() {
		List<IProblem> problems = new ArrayList<IProblem>();
		if(mainProcedure == null)
			problems.add(new Problem(this, "No main procedure is defined"));
		return problems;
	}
}
