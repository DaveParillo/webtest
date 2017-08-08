// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;


import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.WebClient;


/**
 * Enables / disables javascript execution in the HtmlUnit "browser".<p>
 *
 * @author Marc Guillemot
 * @webtest.step category="Core"
 * name="enableJavaScript"
 * alias="enablejavascript"
 * description="Provides the ability to activate / deactivate <key>javascript</key> support. 
 * (Javascript support is enabled by default)"
 * @since Dec 2004
 */
public class EnableJavaScript extends Step {
    private boolean fEnable = true;
    private long fWait = 500L;

    public void doExecute() throws Exception {
        WebClient wc = getContext().getWebClient();
        wc.getOptions().setJavaScriptEnabled(fEnable);
        
        final long endTime = System.currentTimeMillis() + 30000L;
        while (wc.waitForBackgroundJavaScriptStartingBefore(fWait) > 0 && 
                System.currentTimeMillis() < endTime ) {
            wc.waitForBackgroundJavaScriptStartingBefore(fWait); 
        }

    }

    /**
     * @webtest.parameter required="false"
     * default="true"
     * description="Indicates if JavaScript is enabled (true) or disabled (false)."
     */
    public void setEnable(final String enable) {
        fEnable = ConversionUtil.convertToBoolean(enable, false);
    }

    /**
     * @webtest.parameter required="false"
     * default="500 ms"
     * description="Set the wait time, in milliseconds, for background or asynchronous JavaScript to finish."
     */
    public void setWait(final String wait) {
        fWait = ConversionUtil.convertToLong(wait, 500);
;
    }
}
