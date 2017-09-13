package pt.iscte.pandionj.launcher;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;

// TODO enablement refresh
public class TerminateCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator.terminate();

		fireHandlerChanged(new HandlerEvent(this, true, true));
		return null;
	}

	
	@Override
	public boolean isEnabled() {
		return Activator.isExecutingLaunch();
	}
	
	public void setEnabled(Object evaluationContext) {
		setBaseEnabled(isEnabled());
		System.out.println("??" + evaluationContext);
	}
}
