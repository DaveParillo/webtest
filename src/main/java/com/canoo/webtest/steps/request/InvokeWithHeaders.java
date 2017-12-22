package com.canoo.webtest.steps.request;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.canoo.webtest.util.FileUtil;
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
 * 
 * This step is the same as <invoke> except it takes <httpHeader> elements as
 * child elements to define the custom request headers.
 * @author Luke Campbell
 * @webtest.step
 *   category="Core"
 *   name="invokeWithHeaders"
 *   description="This step executes a request to a particular URL."
 */
public class InvokeWithHeaders extends Invocation {
    private static final Logger LOG = Logger.getLogger(InvokeWithHeaders.class);
    /** A list of the nested HttpHeader elements */
    private final List<HttpHeader> headers = new ArrayList<>();

    /**
     * Adds each nested HttpHeader element to our list of headers.
     * @param header The HTTP header
     * @webtest.nested.parameter
     *    required="no"
     *    description="Set HTTP Headers"
     */
    public void addHttpHeader(HttpHeader header) {
        headers.add(header);
    }
    
    public List<HttpHeader> getHeaders() {
        return headers;
    }

    /**
     * Returns the resulting page from the request. This method also updates
     * the request headers with each of the nested HttpHeader elements.
     */
    @Override
    protected Page findTarget() throws IOException, SAXException {
        String method = getMethod();
        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            return findTargetByPost();
        }
        String completeUrl = getContext().getConfig().getUrlForPage(getUrl());
        setCompleteUrl(completeUrl);
        final WebRequest settings = new WebRequest(new URL(completeUrl));
        final Map<String, String> httpHeaders = new HashMap<>();
        for (HttpHeader header : headers) {
            httpHeaders.put(header.getName(), header.getValue());
        }
        settings.setAdditionalHeaders(httpHeaders);
        settings.setHttpMethod(HttpMethod.valueOf(getMethod().toUpperCase()));
        return getResponse(settings);
    }

    /**
     * Returns the resulting page from the POST/PUT/PATCH request. This method
     * also updates the request headers with each of the nested HttpHeader
     * elements.
     */
    @Override
    protected Page findTargetByPost() throws IOException, SAXException {
        String url = getContext().getConfig().getUrlForPage(getUrl());
        final String contentType = getContentType();
        final String soapAction = getSoapAction();
        final String content;
        final WebRequest settings = new WebRequest(new URL(url), HttpMethod.valueOf(getMethod()));
        
        // Store each extra header
        final Map<String, String> requestHeaders = new HashMap<>();
        if (!StringUtils.isEmpty(contentType)) {
            requestHeaders.put("Content-Type", contentType);
        } else {
            // default content-type is an HTML Form
            requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
        }
        if (!StringUtils.isEmpty(soapAction)) {
            requestHeaders.put("SOAPAction", soapAction);
        } 

        // Update the request headers with each of our nested HttpHeader
        // elements.
        for (HttpHeader header : headers) {
            requestHeaders.put(header.getName(), header.getValue());
        }

        settings.setAdditionalHeaders(requestHeaders);

        if (getContent() != null) {
            content = getContent();
        } else {
            content = FileUtil.readFileToString(getContentFile(), this);
        }
        settings.setRequestBody(content);
        return getResponse(settings);
    }

    /**
     * Container for HTTP Headers.
     * @author devel
     * @webtest.nested
     *   category="Core"
     *   name="httpHeader"
     *   description="Container for HTTP Headers
     *   Can be used like 
     *   <invokeWithHeaders ...>
     *     <httpHeader name="X-Extra-Header" value="value" />
     *   </invokeWithHeaders>
     *   "
     */
    public static class HttpHeader {
        private String name;
        private String value;

        public String getName() {
            return name;
        }
        
        public String getValue() {
            return value;
        }
        
        /**
         * Sets the name component of the HTTP Header
         *
         * @param name
         * @webtest.parameter
         *   required="yes"
         *   description="Sets the name component of the HTTP Header"
         */
        public void setName(String name) {
            this.name = name;
        }
        

        /**
         * Sets the name component of the HTTP Header
         *
         * @param value
         * @webtest.parameter
         *   required="yes"
         *   description="Sets the value of the HTTP Header"
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
}
