package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import model.machine.IArray;
import model.machine.IExecutionData;
import model.machine.impl.ProgramState;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.ILoop;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IVariableDeclaration;
import model.program.impl.Factory;

public class TestArrays {

	@Test
	public void testNaturals() {
		IFactory factory = new Factory();
		IProgram program = factory.createProgram();
		
		IProcedure natFunc = program.createProcedure("naturals", new IArrayType.ValueTypeArray(IDataType.INT, 1));
		IVariableDeclaration nParam = natFunc.addParameter("n", IDataType.INT);
		IArrayVariableDeclaration vVar = natFunc.arrayDeclaration("v", factory.arrayType(IDataType.INT, 1));
		vVar.assignment(factory.arrayAllocation(IDataType.INT, nParam.expression()));
		
		IVariableDeclaration iVar = natFunc.variableDeclaration("i", IDataType.INT);
		IExpression e = factory.binaryExpression(IOperator.SMALLER, iVar.expression(), nParam.expression());
		ILoop loop = natFunc.loop(e);
		IBinaryExpression iPlus1 = factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1));
		loop.arrayElementAssignment(vVar, iPlus1, iVar.expression());
		loop.assignment(iVar, factory.binaryExpression(IOperator.ADD, iVar.expression(), factory.literal(1)));
		natFunc.returnStatement(vVar.expression());
		
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
}
