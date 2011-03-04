package com.amplafi.janrain;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthFilter implements Filter {

	public static final String ATTR_ERROR = "error";
	public static final String ATTR_OPEN_ID = "open-id";

	private static final String ENC = "UTF-8";
	private static final String URI_AUTH = "/auth";
	private static final String URI_AUTH_INFO = "https://rpxnow.com/api/v2/auth_info";
	private static final String URI_LOGOUT = "/logout";

	private static final String FIELD_IDENTIFIER = "identifier";
	
	private static final String PARAMETER_API_KEY = "apiKey";
	private static final String PARAMETER_TOKEN = "token";

	private static final String PAGE_SECURED = "/secured.jsp";
	private static final String PAGE_LOGIN = "/index.jsp";

	private String apiKey;
	
	public void init(FilterConfig filterConfig) throws ServletException {
		apiKey = filterConfig.getInitParameter(PARAMETER_API_KEY);
		if(apiKey == null){
			throw new IllegalStateException("You have to configure Janrain. Put janrain.properties onto you classpath and specify yout apiKey there.");
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpSession session = httpServletRequest.getSession();
		String servletPath = httpServletRequest.getServletPath();
		if(servletPath.equals(URI_LOGOUT)){
			doLogout(session);
		}
		if (isAuthenticated(session)) {
			chain.doFilter(httpServletRequest, response);
		} else {
			if (!servletPath.equals(PAGE_LOGIN)) {
				HttpServletResponse httpServletResponse = (HttpServletResponse) response;
				boolean janrainRequest = servletPath.equals(URI_AUTH) && httpServletRequest.getParameterMap().containsKey(PARAMETER_TOKEN);
				if (janrainRequest) {
					try {
						session.setAttribute(ATTR_OPEN_ID, getAuthInfo(httpServletRequest));
						showSecured(httpServletResponse);
					} catch (AuthFailedException e) {
						httpServletResponse.sendError(200, e.getMessage());
					}
				} else {
					showLogin(httpServletResponse);
				}
			} else {
				chain.doFilter(httpServletRequest, response);
			}
		}
	}

	private void showSecured(HttpServletResponse httpServletResponse)throws IOException {
		httpServletResponse.sendRedirect(PAGE_SECURED);
	}

	private void showLogin(HttpServletResponse httpServletResponse) throws IOException {
		httpServletResponse.sendRedirect(PAGE_LOGIN);
	}

	private String encode(String message) throws UnsupportedEncodingException {
		return URLEncoder.encode(message, ENC);
	}

	private String getAuthInfo(HttpServletRequest httpServletRequest) throws IOException, HttpException, AuthFailedException {
		HttpClient client = new HttpClient();
		GetMethod authInfo = new GetMethod(URI_AUTH_INFO);
		String queryString = PARAMETER_API_KEY +	"=" + encode(apiKey) + "&" + PARAMETER_TOKEN + "=" + encode(httpServletRequest.getParameter(PARAMETER_TOKEN));
		authInfo.setQueryString(queryString);
		client.executeMethod(authInfo);
		String authInfoResponse = authInfo.getResponseBodyAsString();
		try {
			JSONObject info = new JSONObject(authInfoResponse);
			info.getString(FIELD_IDENTIFIER);
		} catch (JSONException e) {
			throw new AuthFailedException("Got bad result from Janbrain:\n" + authInfoResponse);
		}
		return authInfoResponse;
	}

	private void doLogout(HttpSession session) {
		session.removeAttribute(ATTR_OPEN_ID);
	}

	private boolean isAuthenticated(HttpSession session) {
		return session.getAttribute(ATTR_OPEN_ID) != null;
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

}
