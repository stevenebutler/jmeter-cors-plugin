package nz.co.breakpoint.jmeter.modifiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.SearchByClass;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class CorsPreProcessor extends AbstractTestElement implements PreProcessor, ThreadListener, TestBean {

    private static final long serialVersionUID = 1L;

    public static Logger log = LoggerFactory.getLogger(CorsPreProcessor.class);

    protected transient ListenerNotifier notifier = new ListenerNotifier();
    protected transient List<SampleListener> listeners;

    public static final String PREFLIGHT_LABEL_SUFFIX = "preflightLabelSuffix";
    public static final String PREFLIGHT_LABEL_SUFFIX_DEFAULT = "-preflight";
    public static final String allowedMethods = "GET|HEAD|POST";
    public static final String forbiddenMethods = "CONNECT|TRACE|TRACK";
    public static final String methodOverrideHeaders = "x-http-method|x-http-method-override|x-method-override";

    final static Map<String, String> safeListedHeaders = new HashMap<>() {{
            put("accept", ".*");
            put("accept-language", ".*");
            put("content-language", ".*");
            put("content-type", "(application/x-www-form-urlencoded|multipart/form-data|text/plain).*");
            put("range", "bytes=[0-9]+-[0-9]*");
    }};

    public static final String forbiddenHeaders = String.join("|",
            "accept-charset",
            "accept-encoding",
            "access-control-request-headers",
            "access-control-request-method",
            "connection",
            "content-length",
            "cookie",
            "cookie2",
            "date",
            "dnt",
            "expect",
            "host",
            "keep-alive",
            "origin",
            "proxy-.*",
            "referer",
            "sec-.*",
            "set-cookie",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "via"
    );

    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        Sampler sampler = context.getCurrentSampler();

        if (!(sampler instanceof HTTPSamplerBase)) {
            return;
        }

        HTTPSamplerBase httpSampler = (HTTPSamplerBase) sampler;
        final String method = httpSampler.getMethod();
        final String preflightHeaders = getPreflightHeaders(httpSampler.getHeaderManager());

        if (method.matches(allowedMethods) && preflightHeaders.isEmpty()) return; // simple request

        HTTPSamplerBase preflight = (HTTPSamplerBase) sampler.clone();
        HeaderManager hm = new HeaderManager();

        hm.add(new Header("Access-Control-Request-Method", method));
        hm.add(new Header("Access-Control-Request-Headers", preflightHeaders));
        hm.removeHeaderNamed("Authorization");

        preflight.setHeaderManager(hm);
        preflight.setMethod("OPTIONS");
        preflight.setName(preflight.getName()+getPreflightLabelSuffix());
        preflight.setThreadContext(context);
        preflight.setThreadName(context.getThread().getThreadName());

        SampleResult result = preflight.sample();
        notifier.notifyListeners(new SampleEvent(result, context.getThreadGroup().getName()), listeners);
    }


    boolean isPreflightHeader(Header h) {
        final String name = h.getName().toLowerCase();

        if (name.matches(forbiddenHeaders)) return false;

        if (name.matches(methodOverrideHeaders)
                && h.getValue().toUpperCase().matches(forbiddenMethods))
            return false;

        String safePattern = safeListedHeaders.get(name);
        if (safePattern != null) {
            return !h.getValue().matches(safePattern);
        }
        return true;
    }

    String getPreflightHeaders(HeaderManager hm) {
        return hm == null ? ""
                : StreamSupport.stream(Spliterators.spliteratorUnknownSize(hm.getHeaders().iterator(), 0), false)
                .map(JMeterProperty::getObjectValue)
                .map(Header.class::cast)
                .filter(this::isPreflightHeader)
                .map(Header::getName)
                .collect(Collectors.joining(","));
    }

    @Override
    public void threadStarted() {
        SearchByClass<SampleListener> listenersSearch = new SearchByClass<>(SampleListener.class);
        getThreadContext().getThread().getTestTree().traverse(listenersSearch);
        listeners = listenersSearch.getSearchResults().stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void threadFinished() {}

    public String getPreflightLabelSuffix() { return getPropertyAsString(PREFLIGHT_LABEL_SUFFIX); }
    public void setPreflightLabelSuffix(String suffix) { setProperty(PREFLIGHT_LABEL_SUFFIX, suffix); }

}
