package pt.iscte.pandionj.parser;

import java.util.List;

public class MethodInfo {
	private final String name;
	private final VisibilityInfo visibility;
	private final String returnType;
	private final List<String> params;
	
	private final boolean noSideEffects;

	public MethodInfo(String name, VisibilityInfo visibility, String returnType, List<String> params, boolean noSideEffects) {
		assert name != null && !name.isEmpty();
		assert visibility != null;
		
		this.name = name;
		this.visibility = visibility;
		this.returnType = returnType;
		this.params = params;
		
		this.noSideEffects = noSideEffects;
	}

	public String getName() {
		return name;
	}
	
	public VisibilityInfo getVisibility() {
		return visibility;
	}
	
	public boolean qualifiesForProperty() {
		return noSideEffects && getNumberOfParameters() == 0;
	}
	
	public int getNumberOfParameters() {
		return params.size();
	}
	

	public String getParameterType(int i) {
		assert i >= 0 && i < getNumberOfParameters();
		return params.get(i);
	}
	
	public String getJNISignature() {
		return genJNISignature(returnType, params);
	}
	
	
	private static String genJNISignature(String returnType, List<String> paramTypes) {
		String params = "";
		for(String p : paramTypes)
			params += toJNI(p);
				
		return "(" + params + ")" + toJNI(returnType);
	}

	//	Z 	boolean
	//	B 	byte
	//	C 	char
	//	S 	short
	//	I 	int
	//	J 	long
	//	F 	float
	//	D 	double
	//	L fully-qualified-class ; 	fully-qualified-class
	//	[ type 	type[] 
	private static String toJNI(String typeName) {
		if(typeName.equals(boolean.class.getName())) return "Z";
		if(typeName.equals(byte.class.getName())) return "B";
		if(typeName.equals(char.class.getName())) return "C";
		if(typeName.equals(short.class.getName())) return "S";
		if(typeName.equals(int.class.getName())) return "I";
		if(typeName.equals(long.class.getName())) return "J";
		if(typeName.equals(float.class.getName())) return "F";
		if(typeName.equals(double.class.getName())) return "D";
		if(typeName.equals(void.class.getName())) return "V";
		
		// TODO arrays
		else return "L" + typeName.replace('.', '/') + ";";
	}


	
}