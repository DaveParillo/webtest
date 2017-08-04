package com.canoo.webtest.engine;

import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * EnableJavaScript Tester.
 *
 * @author Paul King
 * @version 1.0
 * @since <pre>03/05/2005</pre>
 */
public class EnableJavaScriptTest extends BaseStepTestCase {
	private EnableJavaScript fStep;

	public void setUp() throws Exception {
		super.setUp();
		fStep = (EnableJavaScript) getStep();
	}

	protected Step createStep() {
		return new EnableJavaScript();
	}

	public void testDoExecute() throws Exception {
		fStep.setEnable(null);
		executeStep(fStep);
		assertFalse(getContext().getWebClient().getOptions().isJavaScriptEnabled());

		fStep.setEnable("true");
		fStep.setWait("1000");
		executeStep(fStep);
		assertTrue(getContext().getWebClient().getOptions().isJavaScriptEnabled());

		fStep.setEnable("false");
		BaseStepTestCase.executeStep(fStep);
		assertFalse(getContext().getWebClient().getOptions().isJavaScriptEnabled());
	}

}
