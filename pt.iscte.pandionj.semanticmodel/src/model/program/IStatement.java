package model.program;

public interface IStatement extends ISourceElement, IExecutable {
//	String getDescription();
	IBlock getParent();

	default IProcedure getProcedure() {
		IBlock b = getParent();
		while(b != null && !(b instanceof IProcedure))
			b = b.getParent();
		
		return (IProcedure) b;
	}
}
