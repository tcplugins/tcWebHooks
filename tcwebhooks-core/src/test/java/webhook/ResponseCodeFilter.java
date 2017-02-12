package webhook;

//Import required java libraries

import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;

/**
 * Extracts the response code from the response and calls
 * the callback with the response code.
 * <p>
 * This is useful for tests that want to check what the
 * server responded with.
 */

@WebFilter("/*")
public class ResponseCodeFilter implements Filter {

    private ResponseEvent callback;
    Logger logger = LoggerFactory.getLogger(ResponseCodeFilter.class);

    @SuppressWarnings("unused")
    private ResponseCodeFilter() {}

    public ResponseCodeFilter(ResponseEvent callback) {
        this.callback = callback;
    }

    public void init(FilterConfig config) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws java.io.IOException, ServletException {

        // Pass request back down the filter chain
        chain.doFilter(request, response);

        if (callback != null) {
            callback.updateRepsoneCode(((Response) response).getStatus());
        }
        logger.info("Response code is: " + ((Response) response).getStatus());
    }

    public void destroy() {
        /*
		 * Called before the Filter instance is removed from service by the web
		 * container
		 */
    }
}
