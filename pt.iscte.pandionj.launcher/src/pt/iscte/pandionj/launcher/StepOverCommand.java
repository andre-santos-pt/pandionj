package pt.iscte.pandionj.launcher;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class StepOverCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator.stepOver(event);
		return null;
	}

	@Override
	public boolean isEnabled() {
		return Activator.isExecutingLaunch();
	}


}
