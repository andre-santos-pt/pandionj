package pt.iscte.pandionj;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;

import pt.iscte.pandionj.parser.ParserAPI;
import pt.iscte.pandionj.parser.ParserAPI.ParserResult;
import pt.iscte.pandionj.parser2.TagParser;

public class ParserManager {

	private static Map<IFile, ParserResult> cache = new WeakHashMap<>();
	private static Map<IFile, Long> modStamps = new WeakHashMap<>();
	
	private static Map<IFile, TagParser> tagParserCache = new WeakHashMap<>();
	
	public static ParserResult getParserResult(IFile f) {
		ParserResult r = cache.get(f);
		if(r == null || f.getModificationStamp() != modStamps.get(f)) {
			r = ParserAPI.parseFile(f.getRawLocation().toString());
//			ILog log = Platform.getLog(Platform.getBundle(Constants.PLUGIN_ID));
//			log.log(new Status(Status.INFO, Constants.PLUGIN_ID, Status.OK, "Parsed " + f.getLocation().toString() + " " + r.lineExceptions.toString(), null));
			cache.put(f, r);
			modStamps.put(f, f.getModificationStamp());
			
			TagParser tagParser = new TagParser(f, ExtensionManager.validTags());
			tagParser.run();
			tagParserCache.put(f, tagParser);
		}
		return r;
	}
	
	public static Collection<String> getAttributeTags(IFile file, String className, String attName) {
		assert tagParserCache.containsKey(file);
		TagParser tagParser = tagParserCache.get(file);
		return tagParser.getAttributeTags(className, attName);
	}
			
	public static Collection<String> getTags(IFile file, String varName, int line) {
		TagParser tagParser = tagParserCache.get(file);
		if(tagParser == null)
			return Collections.emptyList();
		
		return tagParser.getTags(varName, line);
	}
}
