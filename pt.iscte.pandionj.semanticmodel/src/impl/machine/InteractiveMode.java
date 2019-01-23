package impl.machine;

import java.util.Arrays;
import java.util.Scanner;

import model.machine.IExecutionData;
import model.program.IDataType;
import model.program.IModule;
import model.program.IProcedure;

public class InteractiveMode {

	private IModule program;

	public InteractiveMode(IModule program) {
		this.program = program;
	}


	public void start() {
		ProgramState state = new ProgramState(program);
		Scanner keyboard = new Scanner(System.in);
		String cmd = "";
		do {
			System.out.print("> ");
			cmd = keyboard.nextLine();
			String[] parts = cmd.split("\\s+");
			IDataType[] paramTypes = new IDataType[parts.length-1];
			for(int i = 0; i < paramTypes.length; i++)
				paramTypes[i] = IDataType.INT;

			if(!cmd.equals("exit")) {
				IProcedure p = program.resolveProcedure(parts[0], paramTypes);
				if(p == null)
					System.out.println("procedure not found");
				else {
					IExecutionData data = state.execute(p, Arrays.copyOfRange(parts, 1, parts.length));
					System.out.println(data.getReturnValue());
				}
			}
		}
		while(!cmd.equals("exit"));
		keyboard.close();
	}
}
