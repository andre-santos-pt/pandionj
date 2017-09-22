package pt.iscte.pandionj.extensions.images;

public class ImagesCommon {

	static boolean checkAccept(Object[] values) {
		if(values[0] == null)
			return false;

		int width = ((Object[]) values[0]).length;

		for(int y = 1; y < values.length; y++)
			if(values[y] == null || ((Object[]) values[y]).length != width)
				return false;

		return true;
	}
	
}
