import java.math.BigDecimal;
import java.math.RoundingMode;

import model.machine.impl.ProgramState;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IOperator;
import model.program.IProblem;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProgram;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class Test {
	public static boolean isWhole(BigDecimal bigDecimal) {
	    return bigDecimal.setScale(0, RoundingMode.HALF_UP).compareTo(bigDecimal) == 0;
	}
	
	public static void main(String[] args) {
		double a = 0.02;
	    double b = 0.03;
	    double c = b - a;
	    System.out.println(c);

	    BigDecimal _a = new BigDecimal("1.0");
	    BigDecimal _b = new BigDecimal("3");
	    BigDecimal _c = _a.add(_b);
	    System.out.println(isWhole(_c));
	    
		
		IFactory factory = new Factory();

		IProgram program = factory.createProgram();
		
		IProcedure proc = program.createProcedure("inc", IDataType.INT);
		IVariableDeclaration nParam = proc.addParameter("n", IDataType.INT);
		
		IVariableDeclaration rVar = proc.variableDeclaration("r", program.getDataType("int"));
		IVariableAssignment rAss = rVar.assignment(nParam.expression());
		
		ILiteral lit = factory.literal(4);
		IBinaryExpression e = factory.binaryExpression(IOperator.ADD, rVar.expression(), lit);
		

		IVariableAssignment ass2 = rVar.assignment(e);
		proc.returnStatement(rVar.expression());
		
		IProcedure main = program.createProcedure("main", IDataType.VOID);
//		program.setMainProcedure(main);
		IProcedureCall call = factory.procedureCall(proc, factory.literal(2));
		IVariableDeclaration var2 = main.variableDeclaration("b", IDataType.INT);	
		IVariableAssignment ass3 = var2.assignment(call);
		
		
		for (IProblem iProblem : program.validate()) {
			System.out.println(iProblem);
		}	
		
		ProgramState state = new ProgramState(program);
		
//		state.execute(main);

	}

}
