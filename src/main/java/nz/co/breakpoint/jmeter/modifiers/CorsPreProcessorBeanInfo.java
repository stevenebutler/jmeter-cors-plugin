package nz.co.breakpoint.jmeter.modifiers;

import java.beans.PropertyDescriptor;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import static nz.co.breakpoint.jmeter.modifiers.CorsPreProcessor.PREFLIGHT_LABEL_SUFFIX;
import static nz.co.breakpoint.jmeter.modifiers.CorsPreProcessor.PREFLIGHT_LABEL_SUFFIX_DEFAULT;

public class CorsPreProcessorBeanInfo extends BeanInfoSupport {

    public CorsPreProcessorBeanInfo() {
        super(CorsPreProcessor.class);

        createPropertyGroup("CORS", new String[]{
                PREFLIGHT_LABEL_SUFFIX
        });

        PropertyDescriptor p = property(PREFLIGHT_LABEL_SUFFIX);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, PREFLIGHT_LABEL_SUFFIX_DEFAULT);
    }
}
