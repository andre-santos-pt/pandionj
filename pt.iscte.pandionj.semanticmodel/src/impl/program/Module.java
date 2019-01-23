package impl.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;

import model.program.IConstantDeclaration;
import model.program.IDataType;
import model.program.ILiteral;
import model.program.IModule;
import model.program.IProcedure;
import model.program.IProcedureDeclaration;
import model.program.IStructType;

class Module extends ProgramElement implements IModule {
	private final String id;
	private final List<IConstantDeclaration> constants;
	private final List<IStructType> structs;
	private final List<IProcedure> procedures;
	
	private final List<IProcedure> builtinProcedures;
	
	public Module(String id) {
		this.id = id;
		constants = new ArrayList<>();
		structs = new ArrayList<>();
		procedures = new ArrayList<>();
		
		builtinProcedures = new ArrayList<>();
		builtinProcedures.add(new PrintProcedure());
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
	public Collection<IStructType> getStructs() {
		return Collections.unmodifiableList(structs);
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
	public IStructType addStruct(String id) {
		IStructType struct = new StructType(id);
		structs.add(struct);
		return struct;
	}
	
	@Override
	public IProcedure addProcedure(String id, IDataType returnType) {
		IProcedure proc = new Procedure(id, returnType);
		procedures.add(proc);
		return proc;
	}
	
	@Override
	public IProcedure resolveProcedure(IProcedureDeclaration procedureDeclaration) {
		for(IProcedure p : Iterables.concat(procedures, builtinProcedures))
			if(p.hasSameSignature(procedureDeclaration))
				return p;
		
		return null;
	}
	
	@Override
	public IProcedure resolveProcedure(String id, IDataType... paramTypes) {
		for(IProcedure p : Iterables.concat(procedures, builtinProcedures))
			if(p.matchesSignature(id, paramTypes))
				return p;
		
		return null;
	}
	
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
	
//	@Override
//	public IConstantDeclaration getConstant(String id) {
//		return constants.get(id);
//	}

//	@Override
//	public Collection<IDataType> getDataTypes() {
//		return Collections.unmodifiableCollection(types.values());
//	}
	
//	@Override
//	public IDataType getDataType(String id) {
//		assert types.containsKey(id);
//		return types.get(id);
//	}
}
