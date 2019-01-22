package impl.machine;

import java.util.Arrays;
import java.util.Scanner;

import model.program.IModule;
import model.program.IProcedure;

public class InteractiveMode {

	private IModule program;
	
	public InteractiveMode(IModule program) {
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
