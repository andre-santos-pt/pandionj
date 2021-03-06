package impl.program;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import model.program.IArrayType;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCallExpression;
import model.program.IVariableDeclaration;

class Procedure extends ProgramElement implements IProcedure {
	private final String name;
	private final List<IVariableDeclaration> variables;
	private final List<IVariableDeclaration> variablesView;
	private final ParamsView paramsView;
	private final LocalVariablesView localVarsView;
	private final IDataType returnType;
	private final Block body;
	private int parameters;
	
	public Procedure(String id, IDataType returnType) {
		this.name = id;
		this.variables = new ArrayList<>(5);
		this.parameters = 0;
		this.returnType = returnType;

		variablesView = Collections.unmodifiableList(variables);
		paramsView = new ParamsView();
		localVarsView = new LocalVariablesView();
		body = new Block(this, false);
	}

	@Override
	public String getId() {
		return name;
	}

	@Override
	public List<IVariableDeclaration> getVariables() {
		return variablesView;
	}

	@Override
	public List<IVariableDeclaration> getParameters() {
		return paramsView;
	}
	
	@Override
	public List<IVariableDeclaration> getLocalVariables() {
		return localVarsView;
	}

	@Override
	public IVariableDeclaration addParameter(String id, IDataType type) {
		IVariableDeclaration param = type instanceof IArrayType ?
				new ArrayVariableDeclaration(body, id, (IArrayType) type, ImmutableSet.of()) :
				new VariableDeclaration(body, id, type, ImmutableSet.of());
				
		variables.add(parameters, param);
		parameters++;
		return param;
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

	@Override
	public IBlock getBody() {
		return body;
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
		for(IVariableDeclaration var : variables)
			vars += var +"\n";
		return returnType + " " + name + "(" + params + ")" + "\n" + vars + body.toString();
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

	private class LocalVariablesView extends AbstractList<IVariableDeclaration> {
		@Override
		public IVariableDeclaration get(int index) {
			return variables.get(parameters + index);
		}

		@Override
		public int size() {
			return variables.size() - parameters;
		}
	}

	
	void addVariableDeclaration(IVariableDeclaration var) {
		variables.add(var);
	}
	
	@Override
	public IProcedureCallExpression callExpression(List<IExpression> args) {
		return new ProcedureCallExpression(this, args);
	}
}
