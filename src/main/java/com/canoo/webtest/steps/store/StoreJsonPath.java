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

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.canoo.webtest.engine.StepFailedException;
import com.gargoylesoftware.htmlunit.Page;

/**
 * Stores the result of a JsonPath expression into a property.
 *
 * @author Dave Parillo
 * @webtest.step
 *   category="Core"
 *   name="storeJsonPath"
 *   description="This step stores the result of a JsonPath expression into a property."
 * @since July 2017
 * @see <a href="https://github.com/json-path/JsonPath">Java JsonPath implementation on GitHub</a>
 */
public class StoreJsonPath extends BaseStoreStep {
    private String fJpath;
    private String fDefault;

    /**
     * @webtest.parameter
     * 	required="yes"
     *  description="The JsonPath expression to be evaluated."
     */
    public void setJpath(final String jpath) {
        fJpath = jpath;
    }

    /**
     * @webtest.parameter
     * 	required="no"
     *  description="The value to store in the property when the xpath evaluation returns no result 
     *  (if not set, the step will fail if the JsonPath evaluation returns no result).
     *  Zero length defaults are ignored."
     */
    public void setDefault(final String defaultValue) {
        if (defaultValue.length() > 0) {
            fDefault = defaultValue;
        }
    }

    /**
     * Does the verification work.
     *
     * @see com.canoo.webtest.steps.Step#doExecute()
     */
    @Override
    public void doExecute() {
        storeProperty(evaluateJsonPath());
    }
    
    @Override
    protected void verifyParameters() {
        super.verifyParameters();
        emptyParamCheck(getProperty(), "property");
        emptyParamCheck(fJpath, "jpath");
    }

    private String evaluateJsonPath() {
        final String content = getContent();
        final Object results = getJSON(content);
        return results.toString();
    }

    private String getContent() throws StepFailedException {
        final Page page = getContext().getCurrentResponse();
        if (page == null) {
            throw new StepFailedException("Null page received");
        }
        return page.getWebResponse().getContentAsString();
    }

    private Object getJSON(final String content) throws StepFailedException {
        try {
            return JsonPath.read(content, fJpath);
        } 
        catch (PathNotFoundException e) {
            if (fDefault == null) {
                throw new StepFailedException(e.getMessage());
            }
        }
        return fDefault;
    }

}

