package pt.iscte.pandionj.launcher;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;

public class StepOverCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			for(IThread t : LaunchCommand.launch.getDebugTarget().getThreads())
				if(t.canStepOver())
					t.stepOver();

		} catch (DebugException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return LaunchCommand.launch != null && !LaunchCommand.launch.isTerminated();
	}


}
