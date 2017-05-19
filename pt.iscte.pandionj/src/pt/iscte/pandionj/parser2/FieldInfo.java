package pt.iscte.pandionj.parser2;

public class FieldInfo {
	private String name;
	
	public FieldInfo(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
