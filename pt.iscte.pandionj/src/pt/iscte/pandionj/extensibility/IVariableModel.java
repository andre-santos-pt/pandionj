package pt.iscte.pandionj.extensibility;

import java.util.Collection;
import java.util.List;

import pt.iscte.pandionj.parser.VariableInfo;

public interface IVariableModel extends IObservableModel {
	String getName();
	String getTypeName();
	String getCurrentValue();
	List<String> getHistory();
	
	boolean isDecimal();
	boolean isBoolean();
	boolean isInstance();
	boolean isWithinScope();
	
	VariableInfo getVariableRole();
	Collection<String> getTags();
}
