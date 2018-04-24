// Copyright 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.File;
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
 * <p>
 * This step accepts optional &lt;httpHeader&gt; nested elements
 * to define the custom request headers.
 * </p>
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 * @author Luke Campbell
 * @author Dave Parillo
 * @webtest.step
 *   category="Core"
 *   name="invoke"
 *   description="This step executes a request to a particular URL."
 */
public class InvokePage extends AbstractTargetAction {
    private static final Logger LOG = Logger.getLogger(InvokePage.class);
    /** A list of the nested HttpHeader elements */
    private final List<HttpHeader> headers = new ArrayList<HttpHeader>();

    private String fUrl;
    private String fCompleteUrl;
    private String fMethod = "GET";
    private File fContentFile;
    private String fContent;
    private String fContentType;

    public String getMethod() {
        return fMethod;
    }

    public String getUrl() {
        return fUrl;
    }
    
    public String getContentType() {
        return fContentType;
    }
    
    public String getCompleteUrl() {
        return fCompleteUrl;
    }

    /**
     * Sets the url.
     *
     * @param newUrl the relative or absolute url
     * @webtest.parameter required="yes"
     * description="A complete URL or the 'relative' part of an URL which will be appended to the 'static' parts created from the configuration information defined with the <config> step."
     */
    public void setUrl(final String newUrl) {
        fUrl = newUrl;
    }
    
    public void setCompleteUrl(String completeUrl) {
        fCompleteUrl = completeUrl;
    }
    
    /**
     * Sets the Content Type
     * @param contentType
     * @webtest.parameter 
     *   required="no"
     *   description="Sets the content-type HTTP header for POST, PATCH, and PUT requests"
     *   default="application/x-www-form-urlencoded"
     */
    public void setContentType(final String contentType) {
        fContentType = contentType;
    }

    /**
     * Sets the HTTP Method.
     *
     * Strings that do not map directly to 
     * <a href="http://htmlunit.sourceforge.net/apidocs/com/gargoylesoftware/htmlunit/HttpMethod.html">htmlunit.HttpMethod</a>
     * constants are not supported.
     *
     * @param method
     * @webtest.parameter
     *   required="no"
     *   default="GET"
     *   description="Sets the HTTP Method.
     *
     *   Currently, GET, POST, PUT, DELETE, and PATCH are supported."
     */
    public void setMethod(final String method) {
        fMethod = method;
    }

    public File getContentFile() {
        return fContentFile;
    }

    /**
     * Sets the filename of the request contents.
     *
     * @param contentFile
     * @webtest.parameter
     *   required="no"
     *   description="Filename to extract request contents from. 
     *   Ignored for GET requests. 
     *   Only one of <em>content</em> and <em>contentFile</em> should be set."
     */
    public void setContentFile(final File contentFile) {
        fContentFile = contentFile;
    }

    public String getContent() {
        return fContent;
    }

    /**
     * Sets the request content.
     *
     * @param content
     * @webtest.parameter
     *   required="no"
     *   description="Form data in 'application/x-www-form-urlencoded' format, which will be sent in the body of a POST request. Ignored for GET requests. Only one of <em>content</em> and <em>contentFile</em> should be set."
     */
    public void setContent(final String content) {
        fContent = content;
    }

    private boolean isContentNull() {
      return getContent() == null && getContentFile() == null;
    }

    /**
     * Adds each nested HttpHeader element to our list of headers.
     * @param header The HTTP header
     * @webtest.nested.parameter
     *    required="no"
     *    description="Add additional HTTP Headers to the current invoke."
     */
    public void addHttpHeader(HttpHeader header) {
        headers.add(header);
    }
    
    public List<HttpHeader> getHeaders() {
        return headers;
    }


    @Override
    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getUrl(), "url");
        paramCheck(getContent() != null && getContentFile() != null, 
            "Only one of 'content' and 'contentFile' must be set.");

        if ("POST".equals(fMethod)) {
            paramCheck(isContentNull(), 
                "One of 'content' or 'contentFile' must be set for POST.");
        }
        paramCheck("PUT".equals(fMethod) && isContentNull(), 
            "One of 'content' or 'contentFile' must be set for PUT.");
        paramCheck("PATCH".equals(fMethod) && isContentNull(), 
            "One of 'content' or 'contentFile' must be set for PATCH.");
    }

    /**
     * Returns the resulting page from the request. This method also updates
     * the request headers with each of the nested HttpHeader elements.
     */
    protected Page findTarget() throws IOException, SAXException {
        String method = getMethod();
        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            return findTargetByPost();
        }
        String completeUrl = getContext().getConfig().getUrlForPage(getUrl());
        setCompleteUrl(completeUrl);
        final WebRequest settings = new WebRequest(new URL(completeUrl));
        final Map<String, String> httpHeaders = new HashMap<String, String>();
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
    protected Page findTargetByPost() throws IOException, SAXException {
        String url = getContext().getConfig().getUrlForPage(getUrl());
        final String contentType = getContentType();
        final String content;
        final WebRequest settings = new WebRequest(new URL(url), HttpMethod.valueOf(getMethod()));
        
        // Store each extra header
        final Map<String, String> requestHeaders = new HashMap<String, String>();
        if (!StringUtils.isEmpty(contentType)) {
            requestHeaders.put("Content-Type", contentType);
        } else {
            // default content-type is an HTML Form
            requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
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

    protected String getLogMessageForTarget() {
        return "by URL: " + getUrl();
    }

    /**
     * Adds the computed url if only a part was specified (nice to have in reports)
     */
    @Override
    protected void addComputedParameters(final Map map) {
        super.addComputedParameters(map);
        if (!StringUtils.equals(fUrl, fCompleteUrl)) {
            MapUtil.putIfNotNull(map, "-> complete url", fCompleteUrl);
        }
    }


    
    /**
     * Container for HTTP Headers.
     * @author Luke Campbell
     * @webtest.nested
     *   required="no"
     *   name="httpHeader"
     *   description="Container for HTTP Headers.
     *   Can be used like 
     *   <invoke ...>
     *     <httpHeader name="X-Extra-Header" value="value" />
     *   </invoke>
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


