package impl.program;

import java.util.List;

import com.google.common.collect.ImmutableList;

import model.program.IArrayAllocation;
import model.program.IDataType;
import model.program.IExpression;

public class ArrayAllocation extends SourceElement implements IArrayAllocation {
	private IDataType type;
	private ImmutableList<IExpression> dimensions;
	
	public ArrayAllocation(IDataType type, List<IExpression> dimensions) {
		this.type = type;
		this.dimensions = ImmutableList.copyOf(dimensions);
	}

	@Override
	public IDataType getType() {
		return type;
	}

	@Override
	public List<IExpression> getDimensions() {
		return dimensions;
	}

	@Override
	public String toString() {
		String text = "new " + type;
		for(IExpression e : dimensions)
			text += "[" + e + "]";
		return text;
	}
}
