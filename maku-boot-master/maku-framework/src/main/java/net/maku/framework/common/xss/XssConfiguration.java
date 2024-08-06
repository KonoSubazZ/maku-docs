package net.maku.framework.common.xss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * XSS 配置文件
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 *
 * 首先是过滤。对诸如(script、img、a)等标签进行过滤。
 * 其次是编码。像一些常见的符号，如<>在输入的时候要对其进行转换编码，这样做浏览器是不会对该标签进行解释执行的，同时也不影响显示效果。
 * 最后是限制。通过以上的案例我们不难发现xss攻击要能达成往往需要较长的字符串，因此对于一些可以预期的输入可以通过限制长度强制截断来进行防御。
 */
@Configuration
// 开启EnableConfigurationProperties配置属性 将XssProperties作为bean注入进来

// 用springboot开发的过程中，我们会用到@ConfigurationProperties注解，主要是用来把properties或者yml配置文件转化为bean来使用的，
// 而@EnableConfigurationProperties注解的作用是@ConfigurationProperties注解生效。
// 如果只配置@ConfigurationProperties注解，在IOC容器中是获取不到properties配置文件转化的bean的，
// 当然在@ConfigurationProperties加入注解的类上加@Component也可以使交于springboot管理。
@EnableConfigurationProperties(XssProperties.class)
// 表示只有当配置文件中有maku.xss.enabled=true时，
// 相关的配置才会被激活。如果maku.xss.enabled设置为false或不存在，则该配置将不会被应用。
@ConditionalOnProperty(prefix = "maku.xss", value = "enabled")
public class XssConfiguration {
    private final static PathMatcher pathMatcher = new AntPathMatcher();

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter(XssProperties properties) {

        /**
         * 创建FilterRegistrationBean实例：
         * FilterRegistrationBean<XssFilter> bean = new FilterRegistrationBean<>(); 创建了一个FilterRegistrationBean实例，该类是Spring框架提供的用于注册过滤器的工具类。
         * 泛型参数XssFilter指定了要注册的具体过滤器类型。
         * 设置过滤器：
         * bean.setFilter(new XssFilter(properties, pathMatcher)); 设置了FilterRegistrationBean的filter属性，
         * 这里创建了一个新的XssFilter实例，并传入了XssProperties和pathMatcher作为构造函数的参数。
         * 设置过滤器顺序：
         * bean.setOrder(Integer.MAX_VALUE); 设置了过滤器的执行顺序。在Spring中，过滤器的执行顺序是由其order属性决定的，数值越小优先级越高。
         * 这里设置为Integer.MAX_VALUE，意味着这个过滤器将在所有其他过滤器之后执行。这是因为XSS过滤通常是在请求处理的最后阶段进行，以确保请求数据的安全性。
         * 设置过滤器名称：
         * bean.setName("xssFilter"); 设置了过滤器的名称，这有助于在日志和调试过程中识别过滤器。
         * 返回配置好的FilterRegistrationBean：
         * 最后，方法返回配置好的FilterRegistrationBean实例。这通常会在Spring的配置类中被使用，以便将过滤器注册到Spring容器中。
         *
         */
        FilterRegistrationBean<XssFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new XssFilter(properties, pathMatcher));
        bean.setOrder(Integer.MAX_VALUE);
        bean.setName("xssFilter");

        return bean;
    }

    /**
     * xss过滤，处理json类型的请求
     *
     * 构建 ObjectMapper 实例：Jackson2ObjectMapperBuilder 提供了一种简单的方式来创建和配置 ObjectMapper 对象。
     * 配置定制：允许用户通过方法链的方式对 ObjectMapper 进行各种定制，例如设置日期格式、忽略未知字段、启用或禁用某些特性等。
     * 自动配置：在 Spring Boot 应用中，当检测到 Jackson 相关的依赖时，Jackson2ObjectMapperBuilder 会自动被用于配置 ObjectMapper。
     *
     */
    @Bean
    public ObjectMapper xssFilterObjectMapper(Jackson2ObjectMapperBuilder builder, XssProperties properties) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 注册xss过滤器
        SimpleModule module = new SimpleModule("XssFilterJsonDeserializer");
        // 反序列化 -- 参数 1：反序列化的目标类型 2. 自定义序列化逻辑
        module.addDeserializer(String.class, new XssFilterJsonDeserializer(properties, pathMatcher));
        // 注册JSON反序列化配置
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
