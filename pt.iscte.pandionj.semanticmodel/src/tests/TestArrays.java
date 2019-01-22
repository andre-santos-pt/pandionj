package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IArray;
import model.machine.IExecutionData;
import model.program.IArrayVariableDeclaration;
import model.program.IBinaryExpression;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.ILoop;
import model.program.IModule;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IVariableDeclaration;

public class TestArrays {

	@Test
	public void testNaturals() {
		IFactory factory = new Factory();
		IModule program = factory.createModule("Arrays");
		
		IProcedure natFunc = program.addProcedure("naturals", factory.arrayType(IDataType.INT, 1));
		IVariableDeclaration nParam = natFunc.addParameter("n", IDataType.INT);
		IBlock body = natFunc.getBody();
		IArrayVariableDeclaration vVar = body.addArrayDeclaration("v", factory.arrayType(IDataType.INT, 1));
		vVar.addAssignment(factory.arrayAllocation(IDataType.INT, nParam.expression()));
		
		IVariableDeclaration iVar = body.addVariableDeclaration("i", IDataType.INT);
		IExpression e = factory.binaryExpression(IOperator.SMALLER, iVar.expression(), nParam.expression());
		ILoop loop = body.addLoop(e);
		IBinaryExpression iPlus1 = factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1));
		loop.arrayElementAssignment(vVar, iPlus1, iVar.expression());
		loop.addAssignment(iVar, factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1)));
		body.addReturnStatement(vVar.expression());
		
		System.out.println(program);
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(natFunc, "5");
		IArray array = (IArray) data.getReturnValue();
		for(int i = 0; i < 5; i++)
			assertEquals(new BigDecimal(i+1), array.getElement(i).getValue());
	}
	
	public void testContainsReturn() {
		
	}
	
	public void testContainsBreak() {
		
	}

	public void testReplaceContinue() {
		
	}
	
	public void testMerge() {
		
	}
}
