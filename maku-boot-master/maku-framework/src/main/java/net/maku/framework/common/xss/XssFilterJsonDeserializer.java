package net.maku.framework.common.xss;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.maku.framework.common.utils.HttpContextUtils;
import org.springframework.util.PathMatcher;

import java.io.IOException;

/**
 * xss json过滤
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 */
@AllArgsConstructor
public class XssFilterJsonDeserializer extends JsonDeserializer<String> {
    private final XssProperties properties;
    private final PathMatcher pathMatcher;


    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getValueAsString();
        if (StrUtil.isBlank(value)) {
            return null;
        }

        /**
         * 非Web环境:
         * 当不在Servlet容器或Web应用环境中运行时，HttpServletRequest 通常是不可用的。
         * 例如，在单元测试或独立的命令行应用中，没有实际的HTTP请求发生。
         * 线程切换:
         * 在多线程环境下，如果尝试在非Servlet线程中访问 HttpServletRequest，可能会导致其为 null。
         * 例如，在事件监听器或异步任务中，如果没有正确传递 HttpServletRequest，则可能会遇到这个问题。
         * 过滤器或拦截器:
         * 在某些情况下，如过滤器或拦截器中，如果在请求尚未到达控制器之前就试图获取 HttpServletRequest，可能会导致其为 null。
         */

        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        if (request == null) {
            return value;
        }

        // 判断该URI是否放行
        boolean flag = properties.getExcludeUrls().stream().anyMatch(excludeUrl -> pathMatcher.match(excludeUrl, request.getServletPath()));
        if (flag) {
            return value;
        }

        return XssUtils.filter(value);
    }

    @Override
    public Class<String> handledType() {
        return String.class;
    }
}
