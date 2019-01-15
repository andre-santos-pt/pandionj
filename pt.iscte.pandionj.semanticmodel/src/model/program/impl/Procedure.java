package model.program.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import model.program.IArrayElementAssignment;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.ILoop;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProcedureCallExpression;
import model.program.IReturn;
import model.program.ISelection;
import model.program.IStatement;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;

class Procedure extends SourceElement implements IProcedure {
	private final String name;
	private final List<IVariableDeclaration> variables;
	private final ParamsView paramsView;
	private final VariablesView varsView;
	private final IDataType returnType;
	private final Block body;
	private int parameters;

	public Procedure(String name, IDataType returnType) {
		this.name = name;
		this.variables = new ArrayList<>(5);
		this.parameters = 0;
		this.returnType = returnType;

		paramsView = new ParamsView();
		varsView = new VariablesView();
		body = new Block(this);
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
	public IVariableDeclaration addParameter(String name, IDataType type, Set<IVariableDeclaration.Flag> flags) {
		// TODO review null param
		IVariableDeclaration param = type instanceof IArrayType ?
				new ArrayVariableDeclaration(body, name, (IArrayType) type, ImmutableSet.of()) :
				new VariableDeclaration(body, name, type, flags);
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

	@Override
	public IBlock getBody() {
		return body;
	}

	@Override
	public boolean isFunction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRecursive() {
		// TODO Auto-generated method stub
		return false;
	}

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

		return returnType + " " + name + "(" + params + ")" + body;
	}

	@Override
	public IBlock getParent() {
		return null;
	}

	@Override
	public List<IStatement> getStatements() {
		return body.getStatements();
	}	

	@Override
	public void addStatement(IStatement statement) {
		body.addStatement(statement);

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
	public IBlock block() {
		return body.block();
	}

	@Override
	public IVariableDeclaration variableDeclaration(String name, IDataType type, Set<IVariableDeclaration.Flag> flags) {
		IVariableDeclaration var = body.variableDeclaration(name, type, flags);
		variables.add(var);
		return var;
	}

	@Override
	public IArrayVariableDeclaration arrayDeclaration(String name, IArrayType type, Set<IVariableDeclaration.Flag> flags) {
		IArrayVariableDeclaration var = body.arrayDeclaration(name, type, flags);
		variables.add(var);
		return var;
	}

	@Override
	public IVariableAssignment assignment(IVariableDeclaration var, IExpression exp) {
		return body.assignment(var, exp);
	}
	
	@Override
	public IArrayElementAssignment arrayElementAssignment(IArrayVariableDeclaration var, IExpression exp,
			List<IExpression> indexes) {
		return body.arrayElementAssignment(var, exp, indexes);
	}

	@Override
	public ISelection selection(IExpression expression, IBlock block, IBlock alternativeBlock) {
		return body.selection(expression, block, alternativeBlock);
	}

	@Override
	public ILoop loop(IExpression guard) {
		return body.loop(guard);
	}

	@Override
	public IReturn returnStatement(IExpression expression) {
		return body.returnStatement(expression);
	}

	@Override
	public IProcedureCall procedureCall(IProcedure procedure, List<IExpression> args) {
		return body.procedureCall(procedure, args);
	}

	@Override
	public IProcedureCallExpression callExpression(List<IExpression> args) {
		return new ProcedureCallExpression(this, args);
	}
}
