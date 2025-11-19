package Logistics.App.loggingFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestIdFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestIdFilter.class);
    private static final String MDC_REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws  IOException, ServletException {
        long start = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        MDC.put(MDC_REQUEST_ID, requestId);
        try {
            // parameterized logging — pass args so {} placeholders are filled
            logger.info("Incoming request method={} path={} query={} requestId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    requestId);

            filterChain.doFilter(request, response);

            long timeMs = System.currentTimeMillis() - start;
            logger.info("Completed request status={} method={} path={} timeMs={} requestId={}",
                    response.getStatus(),
                    request.getMethod(),
                    request.getRequestURI(),
                    timeMs,
                    requestId);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
