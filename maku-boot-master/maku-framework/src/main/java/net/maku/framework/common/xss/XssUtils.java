package net.maku.framework.common.xss;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * XSS 过滤工具类
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 *
 *
 * 1.1、简介
 *     jsoup 是一款Java 的HTML解析器，可直接解析某个URL地址、HTML文本内容。它提供了一套非常省力的API，
 * 　可通过DOM，CSS以及类似于jQuery的操作方法来取出和操作数据。
 * 1.2、Jsoup的主要功能
 *     1）从一个URL，文件或字符串中解析HTML
 *     2）使用DOM或CSS选择器来查找、取出数据
 *     3）可操作HTML元素、属性、文本
 *     注意：jsoup是基于MIT协议发布的，可放心使用于商业项目。
 */
public class XssUtils {
    /**
     * 不格式化
     *
     * 定义了一个静态成员变量outputSettings，用于控制输出文档的格式化设置。这里设置了prettyPrint为false，
     * 表示输出时不进行美化（即不添加换行符和缩进）。
     */
    private final static Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);

    /**
     * XSS过滤
     *
     * @param content 需要过滤的内容
     * @return 返回过滤后的内容
     */


    /**
     *Jsoup.clean(content, "", Safelist.relaxed(), outputSettings);: 使用Jsoup库中的clean方法对content进行XSS过滤。
     * 第一个参数: 需要过滤的字符串内容。
     * 第二个参数: 空字符串，表示不允许加载外部实体。
     * 第三个参数: Safelist.relaxed()，这是一个预定义的安全白名单，允许一些基本的HTML标签和属性，但限制了潜在的危险元素。
     * 第四个参数: outputSettings，用于控制输出文档的格式化设置。
     *
     * 示例:
     *Original Content: <script>alert('XSS');</script>
     *Filtered Content: &lt;script&gt;alert(&#x27;XSS&#x27;);&lt;/script&gt;
     *
     */
    public static String filter(String content) {
        return Jsoup.clean(content, "", Safelist.relaxed(), outputSettings);
    }

}