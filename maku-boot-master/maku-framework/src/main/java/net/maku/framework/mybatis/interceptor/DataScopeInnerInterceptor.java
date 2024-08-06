package net.maku.framework.mybatis.interceptor;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;

/**
 * 数据权限
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 *
 * beforeQuery方法是 DataScopeInnerInterceptor 类中的核心方法，它实现了MyBatis-Plus的InnerInterceptor接口的拦截逻辑。
 * 这个方法在MyBatis执行SQL查询之前被调用，允许拦截器修改即将执行的SQL语句。下面是对beforeQuery方法参数的解释：
 *
 * Executor executor：
 * Executor是MyBatis的核心执行器，负责执行SQL语句。拦截器可以通过它获取更多的执行上下文信息，但在beforeQuery方法中，executor主要用于后续的执行过程，而不是直接用于修改SQL。
 *
 * MappedStatement ms：
 * MappedStatement对象包含了执行SQL语句所需的所有信息，如SQL语句的ID、参数映射、结果映射等。它提供了对当前待执行的映射语句的访问，可以用来获取映射语句的详细配置。
 *
 * Object parameter：
 * 这个参数包含了执行SQL语句时传入的参数。它可以是任意类型的对象，包括基本类型、实体对象、Map等。DataScopeInnerInterceptor会检查这个参数，以找到DataScope对象，用于构建数据权限过滤条件。
 *
 * RowBounds rowBounds：
 * RowBounds对象用于分页查询，包含了从哪一行开始查询以及查询多少行的信息。虽然DataScopeInnerInterceptor并不直接使用它，但在分页查询场景下，它可能是存在的。
 *
 * ResultHandler resultHandler：
 * ResultHandler是处理查询结果的接口，MyBatis使用它来处理查询结果。DataScopeInnerInterceptor不直接使用这个参数。
 *
 * BoundSql boundSql：
 * BoundSql对象包含了即将执行的SQL语句以及参数绑定信息。它是beforeQuery方法中最关键的参数之一，因为拦截器需要修改这个对象中的SQL语句，以便添加数据权限过滤条件。
 *
 * 在beforeQuery方法中，DataScopeInnerInterceptor会检查parameter参数中是否存在DataScope对象，如果存在并且有有效的过滤条件，
 * 它会解析当前的SQL语句，添加数据权限的过滤条件，然后重写boundSql中的SQL语句，最终修改后的SQL将在后续的执行过程中使用。
 */
public class DataScopeInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        DataScope scope = getDataScope(parameter);
        // 不进行数据过滤
        if (scope == null || StrUtil.isBlank(scope.getSqlFilter())) {
            return;
        }

        // 拼接新SQL
        String buildSql = getSelect(boundSql.getSql(), scope);

        // 重写SQL
        PluginUtils.mpBoundSql(boundSql).sql(buildSql);
    }

    private DataScope getDataScope(Object parameter) {
        if (parameter == null) {
            return null;
        }

        // 判断参数里是否有DataScope对象
        if (parameter instanceof Map<?, ?> parameterMap) {
            for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue() instanceof DataScope) {
                    return (DataScope) entry.getValue();
                }
            }
        } else if (parameter instanceof DataScope) {
            return (DataScope) parameter;
        }

        return null;
    }

    private String getSelect(String buildSql, DataScope scope) {
        try {
            Select select = (Select) CCJSqlParserUtil.parse(buildSql);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            Expression expression = plainSelect.getWhere();
            if (expression == null) {
                plainSelect.setWhere(new StringValue(scope.getSqlFilter()));
            } else {
                AndExpression andExpression = new AndExpression(expression, new StringValue(scope.getSqlFilter()));
                plainSelect.setWhere(andExpression);
            }

            return select.toString();
        } catch (JSQLParserException e) {
            return buildSql;
        }
    }
}