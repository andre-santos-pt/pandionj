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
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IStruct;

class Program extends SourceElement implements IProgram {
	private List<IProcedure> procedures;
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
	public String toString() {
		String text = "";
		for (IProcedure p : procedures) {
			text += p + "\n\n";
		}
		text = text.replaceAll(";", ";\n");
		text = text.replaceAll("\\{", "{\n");
		return text;
	}
}
