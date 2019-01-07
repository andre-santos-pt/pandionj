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

public class Program implements IProgram {
	private List<IProcedure> procedures;
	private IProcedure mainProcedure;
	private Map<String, IDataType> types;
	
	public Program() {
		procedures = new ArrayList<IProcedure>();
		types = new LinkedHashMap<>();
		types.put("int", new DataType("int", Integer.class));
		types.put("double", new DataType("double", Double.class));
	}
	
	@Override
	public Collection<IConstantDeclaration> getConstants() {
		return ImmutableList.of();
	}

	@Override
	public Collection<IProcedure> getProcedures() {
		return procedures;
	}

	public void addProcedure(IProcedure procedure) {
		assert !procedureExists(procedure);
	}
	
	private boolean procedureExists(IProcedure procedure) {
		for(IProcedure p : procedures)
			if(p.hasSameSignature(procedure))
				return true;
		
		return false;
	}
	
	@Override
	public IProcedure getMainProcedure() {
		return mainProcedure;
	}
	
	public void setMainProcedure(IProcedure mainProcedure) {
		assert mainProcedure == null || procedureExists(mainProcedure);
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
	
	private static class DataType implements IDataType {
		final String id;
		final Class<?> valueType;
		
		public DataType(String id, Class<?> valueType) {
			this.id = id;
			this.valueType = valueType;
		}

		@Override
		public String getIdentifier() {
			return id;
		}

		@Override
		public boolean matches(Object object) {
			return valueType.isInstance(object);
		}
		
		@Override
		public Object match(String literal) {
			try {
				Object obj = valueType.getConstructor(String.class).newInstance(literal);
				return obj;
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public String toString() {
			return id;
		}
	}

	
}
