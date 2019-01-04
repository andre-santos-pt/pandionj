package model.program.impl;

import com.google.common.collect.ImmutableList;

import model.machine.IStackFrame;
import model.program.IBlock;
import model.program.IProcedure;
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
	public void execute(IStackFrame stack) {
		System.out.println(stack.getVariable("val"));
	}

}
