package pt.iscte.pandionj.extensibility;

import java.util.List;

public interface IValueModel extends IVariableModel<Object> {
	String getCurrentValue();
	List<String> getHistory();
	boolean isDecimal();
	boolean isBoolean();
}
