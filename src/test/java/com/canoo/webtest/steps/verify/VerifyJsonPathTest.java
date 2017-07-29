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

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

import org.json.JSONArray; 
import org.json.JSONObject; 

/**
 * Tests for {@link VerifyJsonPath}.
 */
public class VerifyJsonPathTest extends BaseStepTestCase {
    private VerifyJsonPath fStep;
    public static final String PROPERTY_NAME = "result";
    public static final String CONTENT_TYPE = "application/javascript";

    protected void setUp() throws Exception {
        super.setUp();

        fStep = (VerifyJsonPath) getStep();
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
        "            \"price\" : 8.95\n" +
        "         },\n" +
        "         {\n" +
        "            \"category\" : \"fiction\",\n" +
        "            \"author\" : \"Evelyn Waugh\",\n" +
        "            \"title\" : \"Sword of Honour\",\n" +
        "            \"price\" : 12.99\n" +
        "         },\n" +
        "         {\n" +
        "            \"category\" : \"fiction\",\n" +
        "            \"author\" : \"Herman Melville\",\n" +
        "            \"title\" : \"Moby Dick\",\n" +
        "            \"isbn\" : \"0-553-21311-3\",\n" +
        "            \"price\" : 8.99\n" +
        "         },\n" +
        "         {\n" +
        "            \"category\" : \"fiction\",\n" +
        "            \"author\" : \"J. R. R. Tolkien\",\n" +
        "            \"title\" : \"The Lord of the Rings\",\n" +
        "            \"isbn\" : \"0-395-19395-8\",\n" +
        "            \"price\" : 22.99\n" +
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
        return new VerifyJsonPath();
    }

    public void testGetFirstAuthor() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$.store.book[0]['author']");
        fStep.setText("\"Nigel Rees\"");
        executeStep(fStep);
    }
    public void testGetInteger() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$.max-price");
        fStep.setText("10");
        executeStep(fStep);
    }
    public void testGetBoolean() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$.boolean-property");
        fStep.setText("true");
        executeStep(fStep);
    }

    public void testGetNull() throws Exception {
        getContext().setDefaultResponse("{\"a\":10,\"b\":null}", CONTENT_TYPE);

        fStep.setJpath("$..[?($.b == null)]");
        fStep.setText("{\"a\":10,\"b\":null}");
        executeStep(fStep);
    }

    public void testGetTotalIllumination() throws Exception {
      // This test expects values to be within 0.01 of actuals
        String actual = "{\"sun\":{\"apparentRightAscension\":21.12736704393985,\"apparentDeclination\":-16.496663772005643,\"topocentricRightAscension\":21.127466089479952,\"topocentricDeclination\":-16.497388408881868,\"topocentricAltitude\":-49.77586684871005,\"azimuth\":79.92969997321515,\"illuminance\":1.0E-16},\"moon\":{\"apparentRightAscension\":8.550734610482353,\"apparentDeclination\":13.849946553118967,\"topocentricRightAscension\":8.50901684206071,\"topocentricDeclination\":13.529539320645268,\"topocentricAltitude\":40.482866407701614,\"azimuth\":262.1025318594151,\"illuminance\":0.11381744731427149,\"percentIllumination\":\" 99%+\"},\"total\":{\"illuminance\":0.11431744731427158},\"dateTime\":\"1996-02-04T00:00:00\",\"latitude\":30.0,\"longitude\":45.0,\"skyCondition\":\"VISIBLE\"}";

        getContext().setDefaultResponse(actual, CONTENT_TYPE);

        String expected = "{\"sun\":{\"apparentRightAscension\":21.13,\"apparentDeclination\":-16.496,\"topocentricRightAscension\":21.127,\"topocentricDeclination\":-16.497,\"topocentricAltitude\":-49.7758,\"azimuth\":79.929,\"illuminance\":1.0E-16},\"moon\":{\"apparentRightAscension\":8.55,\"apparentDeclination\":13.8499,\"topocentricRightAscension\":8.509,\"topocentricDeclination\":13.529,\"topocentricAltitude\":40.48,\"azimuth\":262.10,\"illuminance\":0.1138,\"percentIllumination\":\" 99%+\"},\"total\":{\"illuminance\":0.1143},\"dateTime\":\"1996-02-04T00:00:00\",\"latitude\":30.0,\"longitude\":45.0,\"skyCondition\":\"VISIBLE\"}";

        fStep.setJpath("$..[?(@.latitude == 30)]");
        fStep.setText(expected);
        executeStep(fStep);
    }

    public void testBook3InOrder() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$.store.book[2]");
        fStep.setText("[ { \"category\" : \"fiction\", \"author\" : \"Herman Melville\", \"title\" : \"Moby Dick\", \"isbn\" : \"0-553-21311-3\", \"price\" : 8.99 } ]");
        executeStep(fStep);
    }
    public void testBook3Mixed() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$.store.book[2]");
        fStep.setText("[{\"category\":\"fiction\",\"price\":8.99,\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\"}]");
        executeStep(fStep);
    }

    // extract exactly 2 elements and compare to object
    public void testBook3Subset() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$..book[2].['isbn','title']");
        fStep.setText("[{\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\"}]");
        executeStep(fStep);
    }

    public void testBook3PriceTolerance0() throws Exception {
        getContext().setDefaultResponse(DOCUMENT, CONTENT_TYPE);

        fStep.setJpath("$..book[2].['title','price']");
        fStep.setText("[{\"title\":\"Moby Dick\",\"price\":9.0 }]");
        fStep.setTolerance("0.99");
        executeStep(fStep);
    }

    public void testGetTolerance1() throws Exception {
        getContext().setDefaultResponse("{\"val\":0.9999}", CONTENT_TYPE);

        fStep.setJpath("$..*");
        fStep.setTolerance("0.1");
        fStep.setText("0.955");
        executeStep(fStep);
        fStep.setText("1.04");
        executeStep(fStep);
    }
    public void testGetTolerance2() throws Exception {
        getContext().setDefaultResponse("{\"val\":0.9999}", CONTENT_TYPE);

        fStep.setJpath("$..*");
        fStep.setTolerance("0.05");
        fStep.setText("0.975");
        executeStep(fStep);
        fStep.setText("1.0249");
        executeStep(fStep);
        fStep.setText("0.9499");
        assertFailOnExecute(fStep, "failed to check double value 0.9494", "");
        fStep.setText("1.055");
        assertFailOnExecute(fStep, "failed to check double value 1.055", "");
    }

    public void testGetTolerance3() throws Exception {
        getContext().setDefaultResponse("{\"val\":1.0}", CONTENT_TYPE);

        fStep.setJpath("$..*");
        // The default is 0.01
        //fStep.setTolerance("0.01");
        fStep.setText("0.995");
        executeStep(fStep);
        fStep.setText("1.004");
        executeStep(fStep);
        fStep.setText("0.994");
        assertFailOnExecute(fStep, "failed to check double value 0.994", "");
        fStep.setText("1.0055");
        assertFailOnExecute(fStep, "failed to check double value 1.0055", "");
    }


    public void testVerifyParameters() throws Exception {
        fStep.setJpath(null);
        assertStepRejectsEmptyParam("jpath", getExecuteStepTestBlock());
    }

}

