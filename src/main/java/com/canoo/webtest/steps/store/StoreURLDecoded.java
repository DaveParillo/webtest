// Copyright 2017 Dave Parillo
// Released under the Apache license v2.0.
package com.canoo.webtest.steps.store;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.tools.ant.BuildException;

/**
 * Step which stores a URLDecoded string into a property.<p>
 *
 * @author Dave Parillo
 * @webtest.step
 *   category="Core"
 *   name="storeURLDecoded"
 *   description="Convert text to it's URLDecoded equivalent and store the result into a property."
 */
public class StoreURLDecoded extends BaseStoreStep {
    private String fText;
    private final String enc = "UTF-8";   // encoding used in URLEncoder 

    /**
     * @webtest.parameter required="yes"
     * description="The text to be url decoded."
     */
    public void setText(final String text) {
        fText = text;
    }

    /**
     * @webtest.parameter required="yes"
     * description="The name of the property that stores the decoded value."
     */
    public void setProperty(final String property) {
        super.setProperty(property);
    }

    public void doExecute() {
        try {
            storeProperty(URLDecoder.decode(fText, enc));
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("Failed to decode " + fText + "Unsupported encoding: " + e.getMessage(), e);  
        }
    }

    protected void verifyParameters() {
        super.verifyParameters();
        emptyParamCheck(fText, "text");
        emptyParamCheck(getProperty(), "property");
    }
}

