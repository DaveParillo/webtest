package com.canoo.webtest.steps.request;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import com.canoo.webtest.util.FileUtil;
import com.canoo.webtest.util.MapUtil;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * Base class for InvokePage and InvokeWithHeaders.
 *
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
 */
public class Invocation extends AbstractTargetAction {
    private String fUrl;
    private String fCompleteUrl;
    private String fMethod = "GET";
    private File fContentFile;
    private String fContent;
    private String fContentType;

    private String fSoapAction;

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

    public String getSoapAction() {
        return fSoapAction;
    }

    /**
     * Sets the SOAP action.
     *
     * @param soapAction
     * @webtest.parameter
     *   required="no"
     *   description="Set the SOAP Action header on SOAP POST requests. 
     *
     *   If set, the contentType attribute must also be set, typically something like: 'text/xml; charset=UTF-8'."
     */
    public void setSoapAction(final String soapAction) {
        fSoapAction = soapAction;
    }

    private boolean isContentNull() {
      return getContent() == null && getContentFile() == null;
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
            paramCheck(fSoapAction != null && fContentType == null, 
                "If 'soapAction' is set, then 'contentType' must be set.");
        }
        paramCheck("PUT".equals(fMethod) && isContentNull(), 
            "One of 'content' or 'contentFile' must be set for PUT.");
        paramCheck("PATCH".equals(fMethod) && isContentNull(), 
            "One of 'content' or 'contentFile' must be set for PATCH.");
    }

    protected Page findTarget() throws IOException, SAXException {
        if ("POST".equals(fMethod) || "PUT".equals(fMethod) || "PATCH".equals(fMethod)) {
            return findTargetByPost();
        }
        fCompleteUrl = getContext().getConfig().getUrlForPage(getUrl());
        final WebRequest settings = new WebRequest(new URL(fCompleteUrl));
        settings.setHttpMethod(HttpMethod.valueOf(getMethod().toUpperCase()));
        return getResponse(settings);
    }

    protected Page findTargetByPost() throws IOException, SAXException {
        String url = getContext().getConfig().getUrlForPage(getUrl());
        final WebRequest settings = new WebRequest(new URL(url), HttpMethod.valueOf(fMethod));
        
        final Map headers = new HashMap();
        if (!StringUtils.isEmpty(fContentType)) {
            headers.put("Content-Type", fContentType);
        }
        else {
            // default content-type is an HTML Form
            headers.put("Content-Type", "application/x-www-form-urlencoded");
        }
        if (!StringUtils.isEmpty(fSoapAction)) {
            headers.put("SOAPAction", fSoapAction);
        } 
        settings.setAdditionalHeaders(headers);
        final String content;
        if (getContent() != null) {
            content = getContent();
        } 
        else {
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
}
