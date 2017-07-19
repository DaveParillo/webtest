// Copyright 2017 Dave Parillo
// Released under the Apache license v2.0.
package com.canoo.webtest.steps.store;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.tools.ant.BuildException;

/**
 * Step which stores a URLEncoded string into a property.<p>
 *
 * @author Dave Parillo
 * @webtest.step
 *   category="Core"
 *   name="storeURLEncoded"
 *   description="Convert text to it's URLEncoded equivalent and store the result into a property."
 */
public class StoreURLEncoded extends BaseStoreStep {
    private String fText;
    private final String enc = "UTF-8";   // encoding used in URLEncoder 

    /**
     * @webtest.parameter required="yes"
     * description="The text to be url encoded."
     */
    public void setText(final String text) {
        fText = text;
    }

    /**
     * @webtest.parameter required="yes"
     * description="The name of the property that stores the encoded value."
     */
    public void setProperty(final String property) {
        super.setProperty(property);
    }

    public void doExecute() {
        try {
            storeProperty(URLEncoder.encode(fText, enc));
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("Failed to encode " + fText + "Unsupported encoding: " + e.getMessage(), e);  
        }
    }

    protected void verifyParameters() {
        super.verifyParameters();
        emptyParamCheck(fText, "text");
        emptyParamCheck(getProperty(), "property");
    }
}

