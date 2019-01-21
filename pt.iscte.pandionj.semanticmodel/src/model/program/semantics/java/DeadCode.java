package model.program.semantics.java;

import java.util.List;

import model.program.IBlock;
import model.program.IInstruction;
import model.program.IProcedure;
import model.program.IProgramElement;
import model.program.IReturn;
import model.program.semantics.ISemanticProblem;
import model.program.semantics.ISemanticRule;

public class DeadCode implements ISemanticRule<IProcedure> {
	
	@Override
	public ISemanticProblem check(IProcedure procedure) {
		List<IInstruction> sequence = procedure.getBody().getInstructionSequence();
		int i = 0;
		for(; i < sequence.size(); i++)
			if(sequence.get(i) instanceof IReturn)
				break;
		
		if(i != sequence.size())
			return ISemanticProblem.create("dead code");
		else
			return null;
	}
}
