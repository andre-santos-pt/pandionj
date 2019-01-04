import com.google.common.collect.ImmutableList;

import model.machine.impl.ProgramState;
import model.machine.impl.Run;
import model.program.IBinaryExpression;
import model.program.IBlock;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;
import model.program.Operator;
import model.program.impl.Factory;
import model.program.impl.PrintProcedure;

public class Test {
	public static void main(String[] args) {
		IFactory factory = new Factory();

		IProcedure proc = factory.createProcedure("testProc", ImmutableList.of());
//		proc.setBody(seq);

		IVariableDeclaration var = factory.createVariableDeclaration(proc, "a", "int");

		
		ILiteral lit = factory.createLiteral("2");
		IVariableAssignment ass = factory.createAssignment(var, lit);
	
		IBlock seq = factory.createBlock(var, ass);

//		Output out = IFactory.createOutput();
//		out.setExpression(var);
		
		IVariableExpression aVar = factory.createVariableExpression(var);
		ILiteral lit3 = factory.createLiteral("2");
		IBinaryExpression e = factory.createBinaryExpression(Operator.EQUAL, aVar, lit3);
		
//		ISelection iff = factory.createSelection(e, factory.createBlock(out));
//		iffblock.getStatements().add(out);
		
//		seq.getStatements().add(iff);
		
//		System.out.println(program.toCode());
		
		IProcedure printProc = new PrintProcedure();
		
		IProgram program = factory.createProgram(ImmutableList.of(proc, printProc), proc, ImmutableList.of());
		ProgramState state = new ProgramState(program);
		
		Run.execute(program);

	}

}
