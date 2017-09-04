package pt.iscte.pandionj.extensibility;

public interface IRuntimeModel extends IObservableModel<IRuntimeModel.Event<?>> {

	public class Event<T> {
		public enum Type {
			NEW_FRAME, NEW_STACK, STEP, TERMINATION, NEW_OBJECT;
		}

		public final Type type;
		public final T arg;

		public Event(Type type, T arg) {
			this.type = type;
			this.arg = arg;
		}
	}
	
	IStackFrameModel getTopFrame();
}
