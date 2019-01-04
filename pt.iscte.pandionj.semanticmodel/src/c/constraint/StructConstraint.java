package c.constraint;

import model.program.IConstaint;
import model.program.IStruct;

public class StructConstraint implements IConstaint<IStruct> {
	@Override
	public boolean valid(IStruct element) {
		return element.getProcedures().isEmpty();
	}
}
