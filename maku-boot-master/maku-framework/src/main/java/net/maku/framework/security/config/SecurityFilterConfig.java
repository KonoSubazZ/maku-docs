package net.maku.framework.security.config;

import lombok.AllArgsConstructor;
import net.maku.framework.security.exception.SecurityAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

/**
 * Spring SecurityFilter 配置文件
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 */
@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityFilterConfig {
    private final OncePerRequestFilter authenticationTokenFilter;
    private final PermitResource permitResource;

    /**
     * spring security认证流程 登录流程
     * 1. 用户提交认证信息(包括用户名 name 和密码 password)
     *
     * 2.1 获取认证信息:
     *      - UsernamePasswordAuthenticationFilter (具体 包括属性 AuthenticationManager 自己定义Bean来注入)
     *      - Spring Security 的 SecurityFilterChain 中的 UsernamePasswordAuthenticationFilter
     *      - 或其他自定义过滤器捕获用户的认证信息。(也可以自定义实现 MyUsernamePasswordAuthenticationFilter, 只需要继承默认实现的过滤器)
     * 2.2 封装认证信息:
     *      - 认证信息被封装进 Authentication 对象中，通常是一个 UsernamePasswordAuthenticationToken 实例。
     *      - Authentication中是什么信息
     *          Principal：用户信息，没有认证时一般是用户名，认证后一般是用户对象
     *          Credentials：用户凭证，一般是密码
     *          Authorities：用户权限
     * 2.3 认证(调用方法 authenticate()) AuthenticationManager提供的 authenticate() 方法来执行认证。
     *
     *
     * 3. 认证管理器处理认证: AuthenticationManager (负责管理多个认证 provider(具体实现提供者))
     *      - AuthenticationManager 负责处理认证逻辑。
     *      - AuthenticationManager 通常会委托给一个或多个 AuthenticationProvider 来执行具体的认证工作。
     *
     * 4. 具体认证处理: 委托认证 AuthenticationProvider
     *      - AuthenticationProvider 这个我暂时理解为会有多个provider 支持几种登录就有几个 provider
     *      - 想要定义自定义的认证方式，需要实现 AuthenticationProvider 接口，并注册到 Spring 容器中。
     *      - 然后根据登录的方式选择对应的provider (还不知道具体怎么实现)
     *
     *      - AuthenticationProvider 需要传入具体的实现逻辑
     *      - AuthenticationProvider 执行认证逻辑，比如与数据库交互验证用户名和密码。
     *      - 如果认证成功，AuthenticationProvider 会返回一个包含用户详细信息的 Authentication 对象。
     *
     * 5. 认证结果
     *      - 成功
     *          - AuthenticationProvider 执行认证逻辑，比如与数据库交互验证用户名和密码。
     *          - 如果认证成功，AuthenticationProvider 会返回一个包含用户详细信息的 Authentication 对象。
     *
     *      - 失败
     *          - 如果认证失败，AuthenticationManager 会抛出一个 AuthenticationException。
     *          - 这个异常会被 ExceptionTranslationFilter 捕获，并根据配置决定如何处理，比如重定向到登录页面或显示错误消息
     *
     * 6. 存储认证结果
     *      - 成功认证后的 Authentication 对象会被存储在 SecurityContextHolder 中。
     *      - SecurityContextHolder 是一个线程局部变量，用于在当前线程中保存认证状态。
     *
     * 7. 认证成功,授权
     *      - Spring Security 使用 AccessDecisionManager 和 AccessDecisionVoter 来决定用户是否有权访问特定资源。
     *      - 在SpringSecurity中，会使用默认的 FilterSecurityInterceptor 来进行权限校验。
     *      - 在FilterSecurityInterceptor中会从 SecurityContextHolder 获取其中的 Authentication ，然后获取其中的权限信息。判断当前用户是否拥有访问当前资源所需的权限。
     *
     *      7.1 修改loadUserByUsername接口 (获取权限数据)将权限数据保存到 Authentication
     *      7.2 修改 SecurityConfig 配置类, 开启基于方法的安全认证机制,默认不开启 @EnableGlobalMethodSecurity(prePostEnabled = true)
     *      7.3 在需要权限验证的方法上添加 @PreAuthorize("hasAuthority('sys:user:list')")
     *
     *
     * 8. 异常处理
     *      spring security 的异常处理流程:
     *      - 在SpringSecurity中，如果我们在认证或者授权的过程中出现了异常会被ExceptionTranslationFilter捕获到。在ExceptionTranslationFilter中会去判断是认证失败还是授权失败出现的异常。
     *      - 如果是 认证过程 中出现的异常会被封装成 AuthenticationException 然后调用AuthenticationEntryPoint对象的方法去进行异常处理。
     *      - 如果是 授权过程 中出现的异常会被封装成 AccessDeniedException 然后调用AccessDeniedHandler对象的方法去进行异常处理。
     *      - 所以如果我们需要自定义异常处理，我们只需要自定义AuthenticationEntryPoint和AccessDeniedHandler然后配置给SpringSecurity即可
     *
     *      统一处理异常,返回一样的 json 数据:
     *      - 1. 扩展 Spring Security异常处理类：AccessDeniedHandler、AuthenticationEntryPoint
     *          - 第一种方案说明：如果系统实现了全局异常处理，那么全局异常首先会获取AccessDeniedException异常，要想Spring Security扩展异常生效，必须在全局异常再次抛出该异常。
     *          - 也就是说异常会首先被 spring boot 的 GlobalExceptionHandler捕获到,而不会被自定义的异常类处理, 如果想要使用自定义的异常类处理，那么需要将全局异常再次抛出。
     *          - @ExceptionHandler(value = AuthenticationException.class)
     *            public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
     *              // 重新抛出AuthenticationException
     *              throw ex;
     *            }
     *      - 2. 在spring boot全局异常统一处理
     *
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 忽略授权的地址列表
        List<String> permitList = permitResource.getPermitList();
        String[] permits = permitList.toArray(new String[0]);

        /*
        添加自定义过滤器以处理特定的认证逻辑。
        设置无状态会话管理，适用于基于令牌的身份验证。
        配置请求授权规则，允许特定的URL和预检请求，其他请求需要经过身份验证。
        处理未经过身份验证的请求时的异常。
        禁用X-Frame-Options头，允许页面被嵌入到<iframe>中。
        禁用CSRF保护，适用于API应用或其他特定场景
         */
        http
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permits).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new SecurityAuthenticationEntryPoint()))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .csrf(AbstractHttpConfigurer::disable)
        ;

        return http.build();
    }

}
