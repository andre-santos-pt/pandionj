package model.program;

public interface IConstaint<E extends ISourceElement> {
	boolean valid(E element);
}
