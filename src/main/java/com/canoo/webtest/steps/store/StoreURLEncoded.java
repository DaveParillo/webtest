// Copyright 2017 Dave Parillo
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
 * @since July 2017
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

    @Override
    public void doExecute() {
        try {
            storeProperty(URLEncoder.encode(fText, enc));
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("Failed to encode " + fText + "Unsupported encoding: " + e.getMessage(), e);  
        }
    }

    @Override
    protected void verifyParameters() {
        super.verifyParameters();
        emptyParamCheck(fText, "text");
        emptyParamCheck(getProperty(), "property");
    }
}

