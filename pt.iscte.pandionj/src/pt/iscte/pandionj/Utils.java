package pt.iscte.pandionj;

import java.util.List;

public interface Utils {

	public static String toSimpleName(String qualifiedName) {
		String name = qualifiedName;
		int dot = name.lastIndexOf('.');
		if(dot != -1) 
			name = name.substring(dot+1);
		
		int dollar = name.lastIndexOf('$');
		if(dollar != -1)
			name = name.substring(dollar+1);
		
		name = name.replaceFirst("", "");
		return name;
	}
	
	public static void stripQualifiedNames(List<String> list) {
		for(int i = 0; i < list.size(); i++)
			list.set(i, toSimpleName(list.get(i)));
	}
}
