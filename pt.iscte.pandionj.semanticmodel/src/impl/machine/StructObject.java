package impl.machine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import model.machine.IStructObject;
import model.machine.IValue;
import model.program.IDataType;
import model.program.IStructType;
import model.program.IVariableDeclaration;

public class StructObject implements IStructObject {

	private final IStructType type;
	private Map<String, IValue> fields;
	
	public StructObject(IStructType type) {
		this.type = type;
		fields = new LinkedHashMap<>();
		for (IVariableDeclaration var : type.getMemberVariables()) {
			fields.put(var.getId(), Value.create(var.getType(), var.getType().getDefaultValue()));
		}
	}
	
	@Override
	public IValue getField(String id) {
		return fields.get(id);
	}
	
	@Override
	public void setField(String id, IValue value) {
		// TODO check id
		fields.replace(id, value);
	}
	
	@Override
	public String toString() {
		String text = "";
		for (Entry<String, IValue> e : fields.entrySet()) {
			text += e.getKey() + " = " + e.getValue() + "\n";
		}
		return text;
	}

	@Override
	public IDataType getType() {
		return type;
	}

	@Override
	public Object getValue() {
		// TODO array?
		return null;
	}
}
