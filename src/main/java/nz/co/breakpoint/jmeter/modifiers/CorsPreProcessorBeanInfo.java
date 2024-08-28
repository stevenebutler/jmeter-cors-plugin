package nz.co.breakpoint.jmeter.modifiers;

import java.beans.PropertyDescriptor;
import org.apache.jmeter.testbeans.BeanInfoSupport;

import static nz.co.breakpoint.jmeter.modifiers.CorsPreProcessor.*;

public class CorsPreProcessorBeanInfo extends BeanInfoSupport {

    public CorsPreProcessorBeanInfo() {
        super(CorsPreProcessor.class);

        createPropertyGroup("Preflight", new String[]{
                PREFLIGHT_LABEL_SUFFIX
        });
        PropertyDescriptor p;

        p = property(PREFLIGHT_LABEL_SUFFIX);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, PREFLIGHT_LABEL_SUFFIX_DEFAULT);

        createPropertyGroup("Cache", new String[]{
                CLEAR_EACH_ITERATION, DEFAULT_CACHE_EXPIRY
        });

        p = property(CLEAR_EACH_ITERATION);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);

        p = property(DEFAULT_CACHE_EXPIRY);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 5L);
    }
}
