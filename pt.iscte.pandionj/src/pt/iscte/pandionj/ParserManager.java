package pt.iscte.pandionj;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;

import pt.iscte.pandionj.parser.ParserAPI;
import pt.iscte.pandionj.parser.ParserAPI.ParserResult;

public class ParserManager {

	private static Map<IFile, ParserResult> cache = new WeakHashMap<>();
	
	public static ParserResult getParserResult(IFile f) {
		ParserResult r = cache.get(f);
		if(r == null) {
			r = ParserAPI.parseFile(f.getRawLocation().toString());
			cache.put(f, r);
		}
		return r;
	}
}
