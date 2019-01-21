import java.util.Collection;
import java.util.List;

import model.program.IBlock;
import model.program.IInstruction;
import model.program.IProgramElement;
import model.program.ISelection;
import model.program.ISelectionWithAlternative;
import model.program.semantics.ISemanticProblem;

public class SelectionRedundancy implements ElementAnalysis<ISelectionWithAlternative> {

	@Override
	public Collection<IAnalsysItem> perform(ISelectionWithAlternative e) {
		IBlock a = e;
		IBlock b = e.getAlternativeBlock();

		if(!a.isEmpty() && b != null && !b.isEmpty()) {
			List<IInstruction> aSeq = a.getInstructionSequence();
			List<IInstruction> bSeq = a.getInstructionSequence();
			int start = 0;
			while(start < Math.min(aSeq.size(), bSeq.size()) && aSeq.get(start).equals(bSeq.get(start)))
				start++;

			int endA = aSeq.size() - 1;
			int endB = bSeq.size() - 1;
			int end = 0;
			while(endA >= 0 && endB >= 0 && aSeq.get(endA).equals(bSeq.get(endB))) {
				end++;
				endA--;
				endB--;
			}
		}
		return null;
	}

}
