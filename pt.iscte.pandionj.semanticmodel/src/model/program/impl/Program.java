package model.program.impl;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import model.program.IConstantDeclaration;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IStruct;

public class Program implements IProgram {
	private ImmutableList<IProcedure> procedures;
	private IProcedure mainProcedure;
	
	public Program(ImmutableList<IProcedure> procedures, IProcedure mainProcedure) {
		assert mainProcedure == null || procedures.contains(mainProcedure);
		this.procedures = procedures;
		this.mainProcedure = mainProcedure;
	}
	
	@Override
	public Collection<IConstantDeclaration> getConstants() {
		return ImmutableList.of();
	}

	@Override
	public Collection<IProcedure> getProcedures() {
		return procedures;
	}

	@Override
	public IProcedure getMainProcedure() {
		return mainProcedure;
	}

	@Override
	public Collection<IStruct> getStructs() {
		return ImmutableList.of();
	}

}
