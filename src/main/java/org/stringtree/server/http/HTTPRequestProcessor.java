package org.stringtree.server.http;

import java.io.IOException;
import java.util.Arrays;

import org.rack4java.Context;
import org.rack4java.Rack;
import org.rack4java.context.MapContext;

public class HTTPRequestProcessor {
	private static final Context<Object> commonEnvironment = new MapContext<Object>()
	    .with(Rack.RACK_VERSION, Arrays.asList(0, 2))
	    .with(Rack.RACK_ERRORS, System.err)
	    .with(Rack.RACK_MULTITHREAD, true)
	    .with(Rack.RACK_MULTIPROCESS, true)
	    .with(Rack.RACK_RUN_ONCE, false);
	private final Rack application;

	public HTTPRequestProcessor(Rack application) {
		this.application = application;
	}

	public Tract request(String method, String path, String protocol, Tract request) throws IOException {
		Collector collector = new StringBuilderCollector();
		@SuppressWarnings("unchecked")
		Context<String> context = new FallbackContext<String>(new MapContext<String>(), common);
		if (null != path) PathInfoParser.setContext(path, context);
		context.put(EmoConstants.MOUNTCONTEXT, mount);
		context.put(EmoConstants.REQUEST_METHOD, method);
//PathInfoParser.dump(context);
		
		String local = (String) context.get(EmoConstants.REQUEST_PATH);

		for (MountedApplication application : applications) {
//System.err.println("considering mount prefix [" + application.prefix + "] against path [" + local + "]");
			if (local.startsWith(application.prefix)) {
				context.put(EmoConstants.MOUNTPOINT, application.prefix);
				context.put(EmoConstants.REQUEST_LOCALPATH, local.substring(application.prefix.length()));
				boolean handled = emo.delegateAndExpand(application.application, context, collector);
				Tract ret = new MapTract();
				ret.put(EmoConstants.RESPONSE_CODE, handled ? "200" : "404");
				ret.setBody(collector.toString());
				return ret;
			}
		}
		Tract ret = new MapTract();
		ret.put(EmoConstants.RESPONSE_CODE, "404");
		ret.setBody("No mounted application matches '" + path + "'");
		return ret;
	}

}
