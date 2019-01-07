package model.program.impl;

import com.google.common.collect.ImmutableList;

import model.machine.ICallStack;
import model.program.IBlock;
import model.program.IProcedure;
import model.program.IStatement;
import model.program.IVariableDeclaration;

public class PrintProcedure implements IProcedure {

	private final ImmutableList<IVariableDeclaration> params = ImmutableList.of(new VariableDeclaration(this, "val", null));
	
	@Override
	public String getIdentifier() {
		return "print";
	}

	@Override
	public ImmutableList<IVariableDeclaration> getParameters() {
		return params;
	}

	@Override
	public IBlock getBody() {
		return null;
	}

	@Override
	public ImmutableList<IVariableDeclaration> getVariables() {
		return null;
	}

	@Override
	public boolean isFunction() {
		return false;
	}

	@Override
	public boolean isRecursive() {
		return false;
	}

	@Override
	public void execute(ICallStack stack) {
		System.out.println(stack.getTopFrame().getVariable("val"));
	}

	@Override
	public void setBody(IBlock body) {
		
	}

	@Override
	public IBlock getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<IStatement> getStatements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSourceCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLine() {
		// TODO Auto-generated method stub
		return 0;
	}

}
