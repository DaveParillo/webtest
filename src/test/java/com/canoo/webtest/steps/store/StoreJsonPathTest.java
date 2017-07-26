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


import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

import org.json.JSONArray; 
import org.json.JSONObject; 

/**
 * Tests for {@link StoreXPath}.
 */
public class StoreJsonPathTest extends BaseStepTestCase {
    private StoreJsonPath fStep;
    public static final String PROPERTY_NAME = "result";
    public static final String CONTENT_TYPE = "application/javascript";

    protected void setUp() throws Exception {
        super.setUp();

        fStep = (StoreJsonPath) getStep();
        fStep.setProperty(PROPERTY_NAME);
    }

    public static final String DOCUMENT = "{\n" +
        "   \"string-property\" : \"string-value\", \n" +
        "   \"int-max-property\" : " + Integer.MAX_VALUE + ", \n" +
        "   \"long-max-property\" : " + Long.MAX_VALUE + ", \n" +
        "   \"boolean-property\" : true, \n" +
        "   \"null-property\" : null, \n" +
        "   \"int-small-property\" : 1, \n" +
        "   \"max-price\" : 10, \n" +
        "   \"store\" : {\n" +
        "      \"book\" : [\n" +
        "         {\n" +
        "            \"category\" : \"reference\",\n" +
        "            \"author\" : \"Nigel Rees\",\n" +
        "            \"title\" : \"Sayings of the Century\",\n" +
        "            \"display-price\" : 8.95\n" +
        "         },\n" +
        "         {\n" +
        "            \"category\" : \"fiction\",\n" +
        "            \"author\" : \"Evelyn Waugh\",\n" +
        "            \"title\" : \"Sword of Honour\",\n" +
        "            \"display-price\" : 12.99\n" +
        "         },\n" +
        "         {\n" +
        "            \"category\" : \"fiction\",\n" +
        "            \"author\" : \"Herman Melville\",\n" +
        "            \"title\" : \"Moby Dick\",\n" +
        "            \"isbn\" : \"0-553-21311-3\",\n" +
        "            \"display-price\" : 8.99\n" +
        "         },\n" +
        "         {\n" +
        "            \"category\" : \"fiction\",\n" +
        "            \"author\" : \"J. R. R. Tolkien\",\n" +
        "            \"title\" : \"The Lord of the Rings\",\n" +
        "            \"isbn\" : \"0-395-19395-8\",\n" +
        "            \"display-price\" : 22.99\n" +
        "         }\n" +
        "      ],\n" +
        "      \"bicycle\" : {\n" +
        "         \"foo\" : \"baz\",\n" +
        "         \"escape\" : \"Esc\\b\\f\\n\\r\\t\\n\\t\\u002A\",\n" +
        "         \"color\" : \"red\",\n" +
        "         \"display-price\" : 19.95,\n" +
        "         \"foo:bar\" : \"fooBar\",\n" +
        "         \"dot.notation\" : \"new\",\n" +
        "         \"dash-notation\" : \"dashes\"\n" +
        "      }\n" +
        "   },\n" +
        "   \"foo\" : \"bar\",\n" +
        "   \"@id\" : \"ID\"\n" +
        "}";

    protected Step createStep() {
        return new StoreJsonPath();
    }

    public void testGetFirstAuthor() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$.store.book[0]['author']");
        executeStep(fStep);
        assertEquals("Nigel Rees", fStep.getComputedValue());
    }

    public void testAllBooks () throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$.store.book[*].title");
        executeStep(fStep);

        JSONArray expected = new JSONArray();
        expected.put("Sayings of the Century");
        expected.put("Sword of Honour");
        expected.put("Moby Dick");
        expected.put("The Lord of the Rings");
        JSONArray actual = new JSONArray(fStep.getComputedValue());
        assertEquals(expected.toString(), fStep.getComputedValue());
    }

    public void testHandleMissingPage() throws Exception {
        getContext().fakeLastResponse(null);
        fStep.setJpath("$..*");
        assertFailOnExecute(fStep, "failed to detect null page", "Null page received");
    }

    public void testHandleUnknownPage() throws Exception {
        getContext().setDefaultResponse("hello", "text/html");
        fStep.setJpath("$.store");
        assertFailOnExecute(fStep, "failed to detect unknown response", "");
    }

    public void testHandleMalformedPage() throws Exception {
        getContext().setDefaultResponse("hello", CONTENT_TYPE);
        fStep.setJpath("$.store");
        assertFailOnExecute(fStep, "failed to detect non-json response", "");
    }

    public void testBadRequest() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("?.store");
        assertFailOnExecute(fStep, "failed to detect bad request", "Missing property in path $['?']");
    }

    public void testNoMatch() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("text_not_in_response");
        assertFailOnExecute(fStep, "failed to detect bad request", "No results for path: $['text_not_in_response']");
    }
    public void testNoMatchWithDefault () throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);
        final String expected = "default_response";

        fStep.setJpath("text_not_in_response");
        fStep.setDefault(expected);
        executeStep(fStep);

        assertEquals(expected, fStep.getComputedValue());
    }

    public void testVerifyParameters() throws Exception {
        fStep.setJpath("foo");
        fStep.setProperty(null);
        assertStepRejectsEmptyParam("property", getExecuteStepTestBlock());

        fStep.setJpath(null);
        fStep.setProperty(PROPERTY_NAME);
        assertStepRejectsEmptyParam("jpath", getExecuteStepTestBlock());
    }

}

