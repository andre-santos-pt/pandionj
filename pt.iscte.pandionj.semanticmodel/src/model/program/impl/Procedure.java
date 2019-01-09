package model.program.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import model.machine.impl.ProgramState;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IProcedureCall;
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
	public String getIdentifier() {
		return name;
	}

	@Override
	public Iterable<IVariableDeclaration> getParameters() {
		return paramsView;
	}

	@Override
	public IVariableDeclaration addParameter(String name, IDataType type) {
		for(IVariableDeclaration v : variables)
			assert !v.getIdentifier().equals(name) : "duplicate variable name";
		
		IVariableDeclaration param = new VariableDeclaration(null, name, type, false);
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
	public IDataType getReturnType() {
		return returnType;
	}

	@Override
	public IBlock getBody() {
		return body;
	}

//	@Override
//	public void setBody(IBlock body) {
//		this.body = body;
//	}
	
	

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
		// TODO Auto-generated method stub
		return name + "()";
	}

	@Override
	public IBlock getParent() {
		return null;
	}

	@Override
	public List<IStatement> getStatements() {
		return body == null ? ImmutableList.of() : body.getStatements();
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
	public IBlock createBlock() {
		return body.createBlock();
	}

	@Override
	public IVariableDeclaration createVariableDeclaration(String name, IDataType type) {
		IVariableDeclaration var = body.createVariableDeclaration(name, type);
		variables.add(var);
		return var;
	}

	@Override
	public IVariableAssignment createAssignment(IVariableDeclaration var, IExpression exp) {
		return body.createAssignment(var, exp);
	}

	@Override
	public ISelection createSelection(IExpression expression, IBlock block, IBlock alternativeBlock) {
		return body.createSelection(expression, block, alternativeBlock);
	}

	@Override
	public IReturn createReturn(IExpression expression) {
		return body.createReturn(expression);
	}

	@Override
	public IProcedureCall createProcedureCall(IProcedure procedure, List<IExpression> args) {
		return body.createProcedureCall(procedure, args);
	}

	@Override
	public IProcedureCall call(List<IExpression> args) {
		return new ProcedureCall(null, this, args);
	}
	
	
}
