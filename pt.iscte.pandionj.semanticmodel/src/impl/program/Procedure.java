package impl.program;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IIdentifiableElement;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;
import model.program.IVariableDeclaration;

class Procedure extends Block implements IProcedure {
	private final String name;
	private final List<IVariableDeclaration> variables;
	private final ParamsView paramsView;
	private final VariablesView varsView;
	private final IDataType returnType;
	private int parameters;

	public Procedure(String id, IDataType returnType) {
		super(null, false);
		assert IIdentifiableElement.isValidIdentifier(id);
		this.name = id;
		this.variables = new ArrayList<>(5);
		this.parameters = 0;
		this.returnType = returnType;

		paramsView = new ParamsView();
		varsView = new VariablesView();
	}

	@Override
	public String getId() {
		return name;
	}

	@Override
	public Iterable<IVariableDeclaration> getParameters() {
		return paramsView;
	}

	@Override
	public IVariableDeclaration addParameter(String id, IDataType type, Set<IVariableDeclaration.Flag> flags) {
		IVariableDeclaration param = type instanceof IArrayType ?
				new ArrayVariableDeclaration(this, id, (IArrayType) type, ImmutableSet.of()) :
				new VariableDeclaration(this, id, type, flags);
		variables.add(parameters, param);
		parameters++;
		return param;
	}

	@Override
	public int getNumberOfParameters() {
		return parameters;
	}

	@Override
	public Iterable<IVariableDeclaration> getVariables(boolean includingParams) {
		return includingParams ? Collections.unmodifiableCollection(variables) : varsView;
	}

	@Override
	public IVariableDeclaration getVariable(String id) {
		for(IVariableDeclaration v : variables)
			if(v.getId().equals(id))
				return v;
		return null;
	}

	@Override
	public IDataType getReturnType() {
		return returnType;
	}

//	@Override
//	public boolean isFunction() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean isRecursive() {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public String toString() {
		String params = "";
		for(IVariableDeclaration p : paramsView) {
			if(!params.isEmpty())
				params += ", ";
			if(p.isReference())
				params += "*";
			params += p.getId();
		}

		String vars = "";
		for(IVariableDeclaration var : getVariables(true))
			vars += var +"\n";
		return returnType + " " + name + "(" + params + ")" + "\n" + vars + super.toString();
	}

	private class ParamsView extends AbstractList<IVariableDeclaration> {
		@Override
		public IVariableDeclaration get(int index) {
			return variables.get(index);
		}

		@Override
		public int size() {
			return parameters;
		}
	}

	private class VariablesView extends AbstractList<IVariableDeclaration> {
		@Override
		public IVariableDeclaration get(int index) {
			return variables.get(parameters + index);
		}

		@Override
		public int size() {
			return variables.size() - parameters;
		}
	}

	@Override
	public IVariableDeclaration addVariableDeclaration(String name, IDataType type, Set<IVariableDeclaration.Flag> flags) {
		IVariableDeclaration var = super.addVariableDeclaration(name, type,flags);
		variables.add(var);
		return var;
	}

	@Override
	public IArrayVariableDeclaration addArrayDeclaration(String name, IArrayType type, Set<IVariableDeclaration.Flag> flags) {
		IArrayVariableDeclaration var = super.addArrayDeclaration(name, type, flags);
		variables.add(var);
		return var;
	}

	@Override
	public IProcedureCallExpression callExpression(List<IExpression> args) {
		return new ProcedureCallExpression(this, args);
	}
}
