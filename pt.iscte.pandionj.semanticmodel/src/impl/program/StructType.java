package impl.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import model.program.IDataType;
import model.program.IIdentifiableElement;
import model.program.IStructAllocation;
import model.program.IStructType;
import model.program.IVariableDeclaration;
import model.program.IVariableDeclaration.Flag;

class StructType extends SourceElement implements IStructType {
	private final String id;
	private final List<IVariableDeclaration> variables;
	
	StructType(String id) {
		assert IIdentifiableElement.isValidIdentifier(id);
		this.id = id;
		this.variables = new ArrayList<>(5);
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public List<IVariableDeclaration> getMemberVariables() {
		return Collections.unmodifiableList(variables);
	}
	
	@Override
	public IVariableDeclaration addMemberVariable(String id, IDataType type, Set<Flag> flags) {
		IVariableDeclaration var = new VariableDeclaration(null, id, type, ImmutableSet.of(Flag.FIELD));
		variables.add(var);
		return var;
	}
	
	@Override
	public boolean matches(Object object) {
		return false;
	}

	@Override
	public boolean matchesLiteral(String literal) {
		return false;
	}

	@Override
	public Object create(String literal) {
		// ?
		return null;
	}

	@Override
	public String toString() {
		String text = "struct " + id + " {";
		for (IVariableDeclaration member : variables) {
			text += member + ";";
		}
		return text + "}";
	}
	
	@Override
	public int getMemoryBytes() {
		int bytes = 0;
		for(IVariableDeclaration v : variables)
			bytes += v.getType().getMemoryBytes();
		return bytes;
	}
	
	@Override
	public IStructAllocation allocationExpression() {
		return new StructAllocation(this);
	}
	
}
