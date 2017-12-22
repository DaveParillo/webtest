// Copyright � 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.canoo.webtest.util.FileUtil;
import com.canoo.webtest.util.MapUtil;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * Performs an initial request to an url and makes the received response
 * available for subsequent steps.
 * <p>
 * The url may be specified as an absolute url (with protocol) that will be used
 * directly or as the end part of an url. In this case the protocol, host, port and basepath
 * properties of the &lt;config&gt; step will be used to build a complete url.
 * </p>
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="invoke"
 *   description="This step executes a request to a particular URL."
 */
public class InvokePage extends Invocation {
    /**
     * Alternative to set the content of a SOAP message.
     * @param txt the content
     * @webtest.nested.parameter
     *    required="no"
     *    description="An alternative way to set the 'url' or the 'content' attribute.
     * When the 'url' attribute is not specified the nested text is considered as the url value.
     * Otherwise this is used to set the 'content' attribute for e.g. large content (properties get evaluated in this content)."
     */
    public void addText(final String txt) {
        final String expandedText = getProject().replaceProperties(txt);
        if (getUrl() == null) {
            setUrl(expandedText);
        }
        else {
            setContent(expandedText);
        }
    }
}
