package pt.iscte.pandionj.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;

import pt.iscte.pandionj.ExtensionManager;

public class ParserManager {

	private static Map<IFile, Long> modStamps = new WeakHashMap<>();
	private static Map<IFile, VarParser> cacheParser = new WeakHashMap<>();
	private static Map<IFile, TagParser> tagParserCache = new WeakHashMap<>();
	
	public static VarParser getVarParserResult(IFile f) {
		VarParser r = cacheParser.get(f);
		if(r == null || f.getModificationStamp() != modStamps.get(f)) {
			r = new VarParser(f.getRawLocation().toOSString());
			r.run();
			cacheParser.put(f, r);
			modStamps.put(f, f.getModificationStamp());
			
			TagParser tagParser = new TagParser(f, ExtensionManager.validTags());
			tagParser.run();
			tagParserCache.put(f, tagParser);
		}
		r.print();
		return r;
	}
	
	
	public static Collection<String> getAttributeTags(IFile file, String className, String attName) {
		getVarParserResult(file); // loads if not loaded
		TagParser tagParser = tagParserCache.get(file);
		return tagParser == null ? Collections.emptyList() : tagParser.getAttributeTags(className, attName);
	}
			
	public static Collection<String> getTags(IFile file, String varName, int line, boolean isField) {
		getVarParserResult(file); // loads if not loaded
		TagParser tagParser = tagParserCache.get(file);
		if(tagParser == null)
			return Collections.emptyList();
		
		return tagParser.getTags(varName, line, isField);
	}
	
	public static long getStamp(IFile file) {
		return modStamps.get(file);
	}
}
