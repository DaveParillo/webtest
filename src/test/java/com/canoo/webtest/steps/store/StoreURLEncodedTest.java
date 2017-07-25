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

import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;


/**
 * Tests for {@link StoreURLEncoded}.
 */
public class StoreURLEncodedTest extends BaseStepTestCase {
	private StoreURLEncoded fStep;
	public static final String PROPERTY_NAME = "result";
	public static final String SPECIAL_CHARS = "`~!@#$%^&()+=[{]}|\\:;'\",<>/?";

	protected void setUp() throws Exception {
		super.setUp();
		fStep = (StoreURLEncoded) getStep();
		fStep.setProperty(PROPERTY_NAME);
	}

	protected Step createStep() {
		return new StoreURLEncoded();
	}

	public void testEncodeXML() throws Exception {
		fStep.setText("<xml><body><h1>hello</h1></body></xml>");
		executeStep(fStep);
		assertEquals("%3Cxml%3E%3Cbody%3E%3Ch1%3Ehello%3C%2Fh1%3E%3C%2Fbody%3E%3C%2Fxml%3E", fStep.getComputedValue());
	}

	public void testEncodeSpecials() throws Exception {
		fStep.setText(SPECIAL_CHARS);
		executeStep(fStep);
		assertEquals("%60%7E%21%40%23%24%25%5E%26%28%29%2B%3D%5B%7B%5D%7D%7C%5C%3A%3B%27%22%2C%3C%3E%2F%3F", fStep.getComputedValue());
	}

	public void testVerifyParameters() throws Exception {
		fStep.setText(SPECIAL_CHARS);
		fStep.setProperty(null);
		assertStepRejectsEmptyParam("property", getExecuteStepTestBlock());

		fStep.setText(null);
		fStep.setProperty(PROPERTY_NAME);
		assertStepRejectsEmptyParam("text", getExecuteStepTestBlock());
	}

}
