package pt.iscte.pandionj.parser;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;

import pt.iscte.pandionj.ExtensionManager;
import pt.iscte.pandionj.extensibility.ITag;

public class ParserManager {

	private static Map<IFile, Long> modStamps = new WeakHashMap<>();
	private static Map<IFile, VarParser> cacheParser = new WeakHashMap<>();
	private static Map<IFile, TagParser> tagParserCache = new WeakHashMap<>();
	
	public static VarParser getVarParserResult(IFile f) {
		VarParser r = cacheParser.get(f);
		if((r == null || f.getModificationStamp() != modStamps.get(f)) && f.getRawLocation() != null) {
			r = new VarParser(f);
			r.run();
			cacheParser.put(f, r);
			modStamps.put(f, f.getModificationStamp());
			
			TagParser tagParser = new TagParser(f, ExtensionManager.validTags());
			tagParser.run();
			tagParserCache.put(f, tagParser);
		}
//		r.print();
		return r;
	}
	
	
	public static ITag getAttributeTag(IFile file, String className, String attName) {
		getVarParserResult(file); // loads if not loaded
		TagParser tagParser = tagParserCache.get(file);
		return tagParser == null ? null : tagParser.getAttributeTag(className, attName);
	}
			
	public static ITag getTag(IFile file, String varName, int line, boolean isField) {
		getVarParserResult(file); // loads if not loaded
		TagParser tagParser = tagParserCache.get(file);
		if(tagParser == null)
			return null;
		
		return tagParser.getTag(varName, line, isField);
	}
	
	public static long getStamp(IFile file) {
		return modStamps.get(file);
	}
}
