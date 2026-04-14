package oth.ics.wtp.relaybackend;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SameSiteCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, new HttpServletResponseWrapper(response) {
            @Override
            public void addHeader(String name, String value) {
                super.addHeader(name, appendSameSite(name, value));
            }

            @Override
            public void setHeader(String name, String value) {
                super.setHeader(name, appendSameSite(name, value));
            }

            private String appendSameSite(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) && value != null
                        && !value.toLowerCase().contains("samesite")) {
                    return value + "; SameSite=None";
                }
                return value;
            }
        });
    }
}
