package net.maku.framework.common.xss;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Xss 过滤器
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 * extends OncePerRequestFilter：
 * 这种方式是在Spring MVC中常用的，OncePerRequestFilter是Spring框架提供的一个抽象类，它确保过滤器的doFilter方法在每个请求中只被调用一次。
 *
 * 这对于避免重复处理同一个请求非常有用，特别是在处理资源密集型任务时。
 *
 * 继承OncePerRequestFilter意味着你可以直接覆盖doFilterInternal方法，而不需要关心线程安全和重复调用的问题，因为这些已经被父类处理好了。
 *
 * implements Filter：
 * 这是Servlet API标准中定义的过滤器接口，它要求实现init、doFilter和destroy三个方法。
 * 实现Filter接口提供了更底层的控制，但同时也意味着你需要自己处理线程安全和可能的重复调用问题。
 * 在Spring框架之外，或者在更老的Web应用中，这种方式更为常见。
 * 选择extends OncePerRequestFilter而不是implements Filter，通常是因为Spring MVC提供了额外的便利性和安全性，特别是对于那些希望在每个请求中只被调用一次的过滤器。此外，Spring MVC还提供了自动注册和配置过滤器的能力，这在使用OncePerRequestFilter时会更加方便。
 * 总之，如果你正在使用Spring MVC并且想要利用其内置的线程安全和请求处理机制，那么继承OncePerRequestFilter是一个更好的选择。如果你需要更底层的控制或者你的应用不基于Spring框架，那么实现Filter接口可能是更适合的选择。
 */
@AllArgsConstructor
public class XssFilter extends OncePerRequestFilter {
    private final XssProperties properties;
    private final PathMatcher pathMatcher;

    /**
     *
     * doFilterInternal方法：
     * 这个方法是OncePerRequestFilter的核心，它会在每个HTTP请求到达应用时被调用。
     * 方法中首先创建了一个XssRequestWrapper对象，这个对象封装了原始的HttpServletRequest，并在其上添加了XSS过滤的逻辑。
     * 然后，通过filterChain.doFilter()方法将封装后的请求和原始响应传递给过滤链中的下一个过滤器，这样就实现了在请求处理前进行XSS过滤的目的。
     */


    /**
     * shouldNotFilter 方法：
     * 在 doFilterInternal 方法被调用之前，shouldNotFilter 方法会被调用。
     * 这个方法用于判断当前请求是否应该被过滤。如果返回 true，则表示该请求不需要进行XSS过滤，doFilterInternal 方法将不会被执行。
     * doFilterInternal 方法：
     * 如果 shouldNotFilter 返回 false，表示请求需要被过滤，那么 doFilterInternal 方法将会被调用。
     * 在这个方法中，创建了一个 XssRequestWrapper 对象，该对象封装了原始的 HttpServletRequest 并在其上添加了XSS过滤的逻辑。
     *
     * 然后，通过 filterChain.doFilter() 方法将封装后的请求和原始响应传递给过滤链中的下一个过滤器。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        // spring boot 防范 XSS 攻击可以使用过滤器，对内容进行转义，过滤。

        // 我暂时理解过滤xss有两种方式 一种是对入参进行过滤转义，一种是对返回结果进行过滤转义。
        // 1. 创建一个XssRequestWrapper对象，这个对象封装了原始的HttpServletRequest，并在其上添加了XSS过滤的逻辑。
        filterChain.doFilter(new XssRequestWrapper(request), response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 如果是json数据，则不处理
        String contentType = request.getContentType();
        if (StrUtil.isBlank(contentType) || StrUtil.startWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        // 放行不过滤的URL
        return properties.getExcludeUrls().stream().anyMatch(excludeUrl -> pathMatcher.match(excludeUrl, request.getServletPath()));
    }

}
