import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import model.program.IBlock;
import model.program.IExpression;
import model.program.ILiteral;
import model.program.IProgram;

public class MagicNumbers implements ElementAnalysis<IProgram> {

	private Multimap<String, IExpression> map = ArrayListMultimap.create();
	
	@Override
	public Collection<IAnalsysItem> perform(IProgram program) {
		program.getProcedures().forEach(p -> p.getBody().accept(new Visitor()));
		
//		map.keys().forEach((lit, exps) -> { if(exps.size() > 1) System.err.println("magic " + lit);});
		return ImmutableList.of();
	}
	
	private class Visitor implements IBlock.IVisitor {
		
		
		@Override
		public void visitExpression(IExpression expression) {
			if(expression instanceof ILiteral && ((ILiteral) expression).getType().isNumeric()) {
				String val = ((ILiteral) expression).getStringValue();
				if(!val.matches("-1|0|1|0.0"))
					map.put(val, expression);
			}
		}
	}
}
