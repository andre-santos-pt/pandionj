package model.program.semantics.java;

import model.program.semantics.Rule;

public class DeadCode extends Rule {
	
//	@Override
//	public ISemanticProblem check(IProcedure procedure) {
//		List<IInstruction> sequence = procedure.getBody().getInstructionSequence();
//		int i = 0;
//		for(; i < sequence.size(); i++)
//			if(sequence.get(i) instanceof IReturn)
//				break;
//		
//		if(i != sequence.size())
//			return ISemanticProblem.create("dead code");
//		else
//			return null;
//	}
}
