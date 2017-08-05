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

package com.canoo.webtest.steps.verify;

import org.apache.log4j.Logger;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.Page;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>A webtest verify step that evaluates JsonPath expressions.</p>
 *
 * <p>This step can either simply verify that a path is true, or that it matches something in particular. 
 *
 * value.</p>
 *
 * @author Dave Parillo
 * @webtest.step
 *   category="Core"
 *   name="verifyJsonPath"
 *   description="This step verifies that an JsonPath expression is true or has a certain value."
 * @since July 2017
 * @see <a href="https://github.com/json-path/JsonPath">JsonPath on github</a>
 */

public class VerifyJsonPath extends Step {
    private static final Logger LOG = Logger.getLogger(VerifyJsonPath.class);
    private String fJpath;
    private String fText;
    private double fTolerance = 0.01;

    private final Configuration CONF = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected value of the JSON path evaluation.
     *   If omitted the step checks that the JsonPath exists and not empty."
     */
    public void setText(String text) {
        fText = text;
    }
    
    /**
     * @param jpath
     * @webtest.parameter
     * 	required="true"
     *  description="The JsonPath expression to evaluate."
     */
    public void setJpath(final String jpath) {
        fJpath = jpath;
    }


    /**
     * @param tolerance
     * @webtest.parameter
     * 	 required="no"
     *   description="The maximum difference allowed between two floating point values in a response.
     *   This value, if set, must be greater than 0.0, otherwise it is ignored.
     *   
     *   Default value = 0.01.
     *   "
     */
    public void setTolerance(String tolerance) {
      double tmp = ConversionUtil.convertToDouble(tolerance, fTolerance);
      if (tmp > 0.00000001) fTolerance = tmp;
    }

    /**
     * Does the verification work.
     *
     * @see com.canoo.webtest.steps.Step#doExecute()
     */
    @Override
    public void doExecute() throws StepFailedException {
        
        checkJsonPath();
    }

    /**
     * Indicates this step performs no action on the browser.
     */
    @Override
    public boolean isPerformingAction() {
        return false;
    }

    @Override
    protected void verifyParameters() {
        super.verifyParameters();
        emptyParamCheck(fJpath, "jpath");
    }

    private void checkJsonPath() {
        final String content = getContent();
        if (fText == null) {
            // check well-formedness only
            isValidJSON(getJSON(content));
        } else {
            compareContent(getJSON(content));
        }
    }

    private void compareContent(final JsonNode actual) {
        if (actual == null) {
            throw new StepFailedException("Null results received");
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode expected = mapper.readTree(fText);
            if (!isEqualTo(actual, expected)) {
               throw new StepFailedException("actual and expected values differ. expected: '"
                       + expected.toString() + "', actual: '" 
                       + actual.toString() + "'.");
            }
        } 
        catch (java.io.IOException e) {
            throw new StepFailedException(e.getMessage());
        }
    }

    // True if the expected Node is a subset of the actual
    private boolean isEqualTo(JsonNode actual, JsonNode expected) {
        Object act = convertJsonElement(actual);
        Object exp = convertJsonElement(expected);
        return act.equals(exp);
    }

    private Object convertJsonElement(JsonNode element) {
        if (element.isObject()) {
            Iterator<String> keys = element.fieldNames();
            Map<String, Object> jsonMap = new HashMap<String, Object>();
            while (keys.hasNext()) {
                String key = keys.next();
                jsonMap.put(key, convertJsonElement(element.get(key)));
            }
            return jsonMap;
        } else if (element.isArray()) {
            Set<Object> jsonSet = new HashSet<Object>();
            for (final JsonNode item : element) {
                jsonSet.add(convertJsonElement(item));
            }
            return jsonSet;
        } else if (element.isFloatingPointNumber()) {
          return (long) Math.round(Double.valueOf(element.toString())/fTolerance);
        } else {
            return element;
        }
    }



    /**
     * Checks result is either a JSON array or Object.
     *
     * @param results
     * @return false if the result is a naked type (not in either [] or {}).
     */
    private boolean isValidJSON(final JsonNode results) throws StepFailedException {
        if( results == null) {
            throw new StepFailedException("Null results received");
        } else if (!results.isArray()) {
            throw new StepFailedException("Response is not well-formed JSON");
        }
        return true;
    }

    private String getContent() throws StepFailedException {
        final Page page = getContext().getCurrentResponse();
        if (page == null) {
            throw new StepFailedException("Null page received");
        }
        return page.getWebResponse().getContentAsString();
    }

    private JsonNode getJSON(final String content) throws StepFailedException {
        try {
            return JsonPath.using(CONF).parse(content).read(fJpath, JsonNode.class);
        } catch (PathNotFoundException e) {
            throw new StepFailedException("Path not found: " + e.getMessage());
        } catch (InvalidJsonException e) {
            throw new StepFailedException("Invalid JSON: " + e.getMessage());
        }
    }


}

