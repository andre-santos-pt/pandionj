package pt.iscte.pandionj.launcher;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;

public class TerminateCommand extends AbstractHandler {

//	public TerminateCommand() {
//		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
//			
//			@Override
//			public void handleDebugEvents(DebugEvent[] events) {
//				for(DebugEvent e : events)
//					if(e.getKind() == DebugEvent.TERMINATE) {
//						fireHandlerChanged(new HandlerEvent(TerminateCommand.this, true, false));
//					}
//			}
//		});
//	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator.terminate(event);
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return Activator.isExecutingLaunch();
	}

}
