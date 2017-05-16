package pt.iscte.pandionj.launcher;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugException;

public class TerminateCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator.terminate();
		return null;
	}

	@Override
	public boolean isEnabled() {
		return Activator.isExecutingLaunch();
	}
	
	
}
