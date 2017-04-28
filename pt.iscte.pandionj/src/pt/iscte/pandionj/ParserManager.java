package pt.iscte.pandionj;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import pt.iscte.pandionj.parser.ParserAPI;
import pt.iscte.pandionj.parser.ParserAPI.ParserResult;

public class ParserManager {

	private static Map<IFile, ParserResult> cache = new WeakHashMap<>();
	private static Map<IFile, Long> modStamps = new WeakHashMap<>();
	
	public static ParserResult getParserResult(IFile f) {
		ParserResult r = cache.get(f);
		if(r == null || f.getModificationStamp() != modStamps.get(f)) {
			r = ParserAPI.parseFile(f.getRawLocation().toString());
			ILog log = Platform.getLog(Platform.getBundle(Constants.PLUGIN_ID));
			log.log(new Status(Status.INFO, Constants.PLUGIN_ID, Status.OK, "Parsed " + f.getLocation().toString() + " " + r.lineExceptions.toString(), null));
			cache.put(f, r);
			modStamps.put(f, f.getModificationStamp());
		}
		return r;
	}
}
