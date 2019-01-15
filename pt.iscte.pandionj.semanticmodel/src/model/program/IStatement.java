package model.program;

public interface IStatement extends ISourceElement, IExecutable {
	IBlock getParent();

	default IProcedure getProcedure() {
		IBlock b = getParent();
		while(b != null && !(b instanceof IProcedure))
			b = b.getParent();
		
		return (IProcedure) b;
	}
	
	default int getDepth() {
		int d = 1;
		IBlock b = getParent();
		while(b != null && !(b instanceof IProcedure)) {
			b = b.getParent();
			d++;
		}
		return d;
	}
}
