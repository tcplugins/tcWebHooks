package webhook.teamcity.auth.bearer;
import java.io.Serializable;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.ContextAwareAuthScheme;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;

public class BearerAuthSchemeFactory implements AuthSchemeProvider {

    @Override
    public AuthScheme create(HttpContext params) {
        return new BearerAuthScheme();
    }

    public static class BearerAuthScheme implements ContextAwareAuthScheme, Serializable {
		private static final long serialVersionUID = 724596742375237387L;
		private boolean complete = false;

        @Override
        public void processChallenge(Header header) throws MalformedChallengeException {
            this.complete = true;
        }

        @Override
        public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
            return authenticate(credentials, request, null);
        }

        @Override
        public Header authenticate(Credentials credentials, HttpRequest request, HttpContext httpContext)
                throws AuthenticationException {
            CharArrayBuffer buffer = new CharArrayBuffer(32);
            buffer.append(AUTH.WWW_AUTH_RESP);
            buffer.append(": Bearer ");
            buffer.append(credentials.getUserPrincipal().getName());
            return new BufferedHeader(buffer);
        }

        @Override
        public String getSchemeName() {
            return "Bearer";
        }

        @Override
        public String getParameter(String name) {
            return null;
        }

        @Override
        public String getRealm() {
            return null;
        }

        @Override
        public boolean isConnectionBased() {
            return false;
        }

        @Override
        public boolean isComplete() {
            return this.complete;
        }
    }
}