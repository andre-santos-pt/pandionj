package impl.machine;

import java.util.Arrays;
import java.util.Scanner;

import model.program.IProcedure;
import model.program.IProgram;

public class InteractiveMode {

	private IProgram program;
	
	public InteractiveMode(IProgram program) {
		this.program = program;
	}
	
	public void enterInteractiveMode() {
		ProgramState state = new ProgramState(program);
		Scanner keyboard = new Scanner(System.in);
		String cmd = "";
		do {
			cmd = keyboard.nextLine();
			String[] parts = cmd.split("\\s+");
			for (IProcedure p : program.getProcedures()) {
				if(p.getId().equals(parts[0]) && 
						p.getNumberOfParameters() == parts.length-1) {
					state.execute(p, Arrays.copyOfRange(parts, 1, parts.length));
					break;
				}
					
			}
		}
		while(!cmd.equals("exit"));
		keyboard.close();
	}
}
