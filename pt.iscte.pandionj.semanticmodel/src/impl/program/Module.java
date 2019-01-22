package impl.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IConstantDeclaration;
import model.program.IDataType;
import model.program.ILiteral;
import model.program.IModule;
import model.program.IProcedure;
import model.program.IStructType;

class Module extends ProgramElement implements IModule {
	private final String id;
	private final List<IConstantDeclaration> constants;
	private final List<IStructType> structs;
	private final List<IProcedure> procedures;
	
	public Module(String id) {
		this.id = id;
		constants = new ArrayList<>();
		structs = new ArrayList<>();
		procedures = new ArrayList<IProcedure>();
		
		procedures.add(new PrintProcedure());
		procedures.add(new RandomFunction());
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public Collection<IConstantDeclaration> getConstants() {
		return Collections.unmodifiableList(constants);
	}

	@Override
	public Collection<IProcedure> getProcedures() {
		return Collections.unmodifiableList(procedures);
	}
	
	@Override
	public IConstantDeclaration addConstant(String id, IDataType type, ILiteral value) {
		assert id != null;
		assert type != null;
		assert value != null;
		ConstantDeclaration dec = new ConstantDeclaration(this, id, type, value);
		constants.add(dec);
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
		structs.add(struct);
		return struct;
	}
	
//	@Override
//	public IConstantDeclaration getConstant(String id) {
//		return constants.get(id);
//	}
	
	@Override
	public Collection<IStructType> getStructs() {
		return ImmutableList.of();
	}

//	@Override
//	public Collection<IDataType> getDataTypes() {
//		return Collections.unmodifiableCollection(types.values());
//	}
	
//	@Override
//	public IDataType getDataType(String id) {
//		assert types.containsKey(id);
//		return types.get(id);
//	}
	
	@Override // TODO pretty print
	public String toString() {
		String text = "";
		for(IConstantDeclaration c : constants)
			text += c + ";\n";
		
		for(IStructType s : structs)
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
