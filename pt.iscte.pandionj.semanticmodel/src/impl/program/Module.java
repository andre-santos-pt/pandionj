package impl.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import model.program.IConstantDeclaration;
import model.program.IDataType;
import model.program.IIdentifiableElement;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IModule;
import model.program.IStructType;

class Module extends ProgramElement implements IModule {
	private final String id;
	private final Map<String, IConstantDeclaration> constants;
	private final Map<String, IStructType> structs;
	private final List<IProcedure> procedures;
	private final Map<String, IDataType> types;
	
	public Module(String id) {
		this.id = id;
		constants = new LinkedHashMap<String, IConstantDeclaration>();
		structs = new LinkedHashMap<String, IStructType>();
		procedures = new ArrayList<IProcedure>();
		types = new LinkedHashMap<>();
		for(IDataType t : IDataType.DEFAULTS)
			types.put(t.getId(), t);
		
		procedures.add(new PrintProcedure());
		procedures.add(new RandomFunction());
	}
	
	@Override
	public String getId() {
		return id;
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
	public IConstantDeclaration addConstant(String id, IDataType type, ILiteral value) {
		assert id != null;
		assert type != null;
		assert value != null;
		ConstantDeclaration dec = new ConstantDeclaration(this, id, type, value);
		constants.put(id, dec);
		return dec;
	}
	
	@Override
	public IProcedure addProcedure(String id, IDataType returnType) {
		IProcedure proc = new Procedure(id, returnType);
		procedures.add(proc);
		return proc;
	}
	
	@Override
	public IStructType addStruct(String id) {
		IStructType struct = new StructType(id);
		structs.put(id, struct);
		return struct;
	}
	
	@Override
	public IConstantDeclaration getConstant(String id) {
		return constants.get(id);
	}
	
	@Override
	public Collection<IStructType> getStructs() {
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
	
	@Override // TODO pretty print
	public String toString() {
		String text = "";
		for(IConstantDeclaration c : constants.values())
			text += c + ";\n";
		
		for(IStructType s : structs.values())
			text += s + "\n";
		
		for (IProcedure p : procedures) {
			if(!p.isBuiltIn())
				text += p + "\n\n";
		}
		text = text.replaceAll(";", ";\n"); // wrong replacement of tabs
		text = text.replaceAll("\\{", "{\n");
		text = text.replaceAll("\\}", "}\n");
		return text;
	}
}
