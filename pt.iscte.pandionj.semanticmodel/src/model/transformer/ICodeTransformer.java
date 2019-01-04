package model.transformer;

import model.program.IProcedure;
import model.program.IProgram;

public interface ICodeTransformer {

	default StringBuffer getCode(IProgram program) {
		StringBuffer buffer = new StringBuffer();
		program.getProcedures().forEach(p -> generateProcedure(buffer, p));
		return buffer;
	}
	
	void generateProcedure(StringBuffer buffer, IProcedure procedure);
}
