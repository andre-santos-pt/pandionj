import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import model.program.IBlock;
import model.program.ILiteral;
import model.program.IModule;

public class MagicNumbers implements ElementAnalysis<IModule> {

	private Multimap<String, ILiteral> map = ArrayListMultimap.create();
	
	@Override
	public Collection<IAnalsysItem> perform(IModule program) {
		program.getProcedures().forEach(p -> p.getBody().accept(new Visitor()));
		
//		map.keys().forEach((lit, exps) -> { if(exps.size() > 1) System.err.println("magic " + lit);});
		return ImmutableList.of();
	}
	
	private class Visitor implements IBlock.IVisitor {
		@Override
		public void visit(ILiteral lit) {
			if(lit.getType().isNumeric()) {
				String val = lit.getStringValue();
				if(!val.matches("-1|0|1|0.0"))
					map.put(val, lit);
			}
		}
	}
}
