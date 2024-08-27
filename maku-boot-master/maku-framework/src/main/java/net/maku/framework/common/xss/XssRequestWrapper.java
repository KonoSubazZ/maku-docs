package net.maku.framework.common.xss;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * XSS Request Wrapper
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 *
 *
 * HttpServletRequestWrapper是一个非常有用的类，它用于包装HttpServletRequest对象，
 * 允许你在不改变原始请求的情况下修改或增强请求数据。以下是HttpServletRequestWrapper的主要作用：
 * (不改变原始请求,io只能使用一次作用不知道, 使用HttpServletRequestWrapper避免修改请求流导致请求不可用 [存疑])
 *
 * 请求数据的修改:
 * 你可以覆盖HttpServletRequestWrapper中的方法来修改请求参数、请求头等数据。
 * 例如，在上面提供的代码示例中，XssRequestWrapper覆盖了getParameter, getParameterValues, getParameterMap, 和 getHeader 方法，用于对请求参数和头部进行XSS过滤。
 *
 * 请求数据的增强:
 * 你可以添加额外的数据到请求中，例如添加新的请求参数或修改请求头。
 * 这对于添加安全过滤、日志记录或其他中间件功能非常有用。
 *
 * 解决特定问题:
 * 例如，解决字符编码问题。在提供的过滤器代码示例中，ContentTypeFilter通过设置请求和响应的字符编码来解决中文乱码问题。
 * 可以通过覆盖HttpServletRequestWrapper的方法来解决特定的应用场景问题。
 *
 * 统一处理:
 * 通过使用HttpServletRequestWrapper，可以在多个地方复用相同的处理逻辑，避免在不同的地方重复编写相同的代码。
 *
 * 安全性增强:
 * 如上面提供的XssRequestWrapper示例所示，可以用来过滤XSS攻击，保护应用免受恶意脚本注入。
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }


    /**
     * 获取单个请求参数的值。
     * @param name
     * @return
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);

        return filterXss(value);
    }

    /**
     * 获取多个请求参数的值。
     * @param name
     * @return
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] parameters = super.getParameterValues(name);
        if (parameters == null || parameters.length == 0) {
            return null;
        }

        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = filterXss(parameters[i]);
        }
        return parameters;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        Map<String, String[]> parameters = super.getParameterMap();
        for (String key : parameters.keySet()) {
            String[] values = parameters.get(key);
            for (int i = 0; i < values.length; i++) {
                values[i] = filterXss(values[i]);
            }
            map.put(key, values);
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return filterXss(value);
    }

    private String filterXss(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }

        return XssUtils.filter(content);
    }

}