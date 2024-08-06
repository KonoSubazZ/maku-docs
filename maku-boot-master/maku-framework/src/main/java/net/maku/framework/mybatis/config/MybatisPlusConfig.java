package net.maku.framework.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import net.maku.framework.mybatis.handler.FieldMetaObjectHandler;
import net.maku.framework.mybatis.interceptor.DataScopeInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis-plus 配置
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 *
 * 在Spring Boot应用中，MybatisPlusInterceptor及其内部拦截器(InnerInterceptor)的作用机制和工作流程如下：
 *
 * 1. MybatisPlusInterceptor初始化
 * 当Spring容器启动时，MybatisPlusConfig类中的mybatisPlusInterceptor()方法会被调用，创建MybatisPlusInterceptor的实例并配置各种内部拦截器。
 * 这些拦截器通过addInnerInterceptor方法添加到MybatisPlusInterceptor中。
 *
 * 2. 拦截器注册
 * 在MyBatis-Plus的配置阶段，MybatisPlusInterceptor会被注册到SqlSessionFactory中，这意味着所有通过SqlSessionFactory创建的SqlSession实例都会受到这个拦截器的影响。
 *
 * 3. SQL执行流程
 * 当应用程序执行任何SQL操作（如查询、插入、更新或删除）时，MyBatis会通过SqlSession来执行SQL语句。在这个过程中，MybatisPlusInterceptor会拦截这些操作。
 *
 * 4. 拦截器执行
 * 对于每一个SQL操作，MybatisPlusInterceptor中的内部拦截器会按照它们在addInnerInterceptor方法中添加的顺序依次执行。
 * 每个内部拦截器都有机会修改即将执行的SQL语句或其参数，或者在操作前后执行某些逻辑。
 * 具体到内部拦截器：
 * DataScopeInnerInterceptor：在SQL执行前，根据当前用户的权限，动态修改SQL语句，加入数据范围过滤条件，确保用户只能访问自己有权访问的数据。
 * PaginationInnerInterceptor：在执行查询之前，自动添加分页逻辑到SQL语句中，使得查询结果符合分页需求，而无需在业务代码中手动添加分页语句。
 * OptimisticLockerInnerInterceptor：在执行更新操作时，检查实体上的版本字段是否与数据库中的版本匹配，如果不匹配，则抛出异常，防止并发更新的问题。
 * BlockAttackInnerInterceptor：在执行更新或删除操作时，检查SQL语句是否尝试更新或删除所有记录，如果是，则阻止这种操作，防止全表更新或删除带来的数据风险。
 *
 * 5. SQL执行
 * 经过所有拦截器的处理后，最终的SQL语句会被执行，结果会被返回给调用者。
 *
 * 6. 异常处理
 * 如果在拦截器执行过程中或SQL执行过程中发生异常，拦截器也可以捕获这些异常并进行相应的处理。
 * 通过上述流程，MyBatis-Plus的MybatisPlusInterceptor及其内部拦截器能够在不修改业务代码的情况下，增强SQL执行的功能性和安全性。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 数据权限
        mybatisPlusInterceptor.addInnerInterceptor(new DataScopeInnerInterceptor());
        // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 乐观锁
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防止全表更新与删除
        mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return mybatisPlusInterceptor;
    }

    @Bean
    public FieldMetaObjectHandler fieldMetaObjectHandler(){
        return new FieldMetaObjectHandler();
    }
}
