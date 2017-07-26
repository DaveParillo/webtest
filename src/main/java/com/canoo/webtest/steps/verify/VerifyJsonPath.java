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

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.Page;
import org.json.JSONArray; 
import org.json.JSONObject; 
import org.json.JSONException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;

import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider;
import com.jayway.jsonpath.spi.mapper.JsonOrgMappingProvider;

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
            .mappingProvider(new JsonOrgMappingProvider())
            .jsonProvider(new JsonOrgJsonProvider())
            .options(Option.ALWAYS_RETURN_LIST)
            .build();

    /**
     * @param text
     * @webtest.parameter
     * 	 required="no"
     *   description="The expected value of the JSON path evaluation.
     *   If omitted the step checks that the JsonPath exists and not empty."
     */
    public void setText(String text) {
        if (text.trim().charAt(0) == '[') {
            fText = text;
        } else {
            fText = "[" + text + "]";
        }
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
     *   This value, if set, must be &gt; 0.0, otherwise it is ignored.
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

    private void compareContent(final Object actual) {
        if (actual == null) {
            throw new StepFailedException("Null results received");
        }
        try {
            JSONArray expected = new JSONArray(fText);
            if (!areEqual(actual, expected)) {
               throw new StepFailedException("actual and expected values differ. expected: '"
                       + expected.toString() + "', actual: '" 
                       + actual.toString() + "'.");
            }
        } 
        catch (org.json.JSONException e) {
            throw new StepFailedException(e.getMessage());
        }
    }

    private boolean areEqual(Object actual, Object expected) throws JSONException {
        Set<Object> actualSet   = (Set<Object>) convertJsonElement(actual);
        Set<Object> expectedSet = (Set<Object>) convertJsonElement(expected);
        //System.out.println("actualSet: " + actualSet.toString()+ ", class: " + actualSet.getClass().getSimpleName());
        //System.out.println("expectedSet: " + expectedSet.toString()+ ", class: " + expectedSet.getClass().getSimpleName());

        return actualSet.containsAll(expectedSet);
    }

    private Object convertJsonElement(Object elem) throws JSONException {
        if (elem instanceof JSONObject) {
            JSONObject obj = (JSONObject) elem;
            Iterator<String> keys = obj.keys();
            Map<String, Object> jsonMap = new HashMap<String, Object>();
            while (keys.hasNext()) {
                String key = keys.next();
                jsonMap.put(key, convertJsonElement(obj.get(key)));
            }
            return jsonMap;
        } else if (elem instanceof JSONArray) {
            JSONArray arr = (JSONArray) elem;
            Set<Object> jsonSet = new HashSet<Object>();
            for (int i = 0; i < arr.length(); i++) {
                jsonSet.add(convertJsonElement(arr.get(i)));
            }
            return jsonSet;
        } else if (elem instanceof Double) {
          long val = (long) Math.round(Double.valueOf(elem.toString())/fTolerance);
            System.out.println("converted "+ elem + " to : " + val);
          return val; //(long) Math.round(Double.valueOf(elem.toString())/0.01);
        } else {
            return elem;
        }
    }



    /**
     * Checks result is either a JSON array or Object.
     *
     * @param results
     * @return false if the result is a naked type (not in either [] or {}).
     */
    private boolean isValidJSON(final Object results) throws StepFailedException {
        if( results == null) {
            throw new StepFailedException("Null results received");
        } else if (!(results instanceof JSONArray)) {
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

    private Object getJSON(final String content) throws StepFailedException {
        try {
            //return JsonPath.read(content, fJpath);
            return JsonPath.using(CONF).parse(content).read(fJpath);
        } catch (PathNotFoundException e) {
            throw new StepFailedException("Path not found: " + e.getMessage());
        }
    }


}

