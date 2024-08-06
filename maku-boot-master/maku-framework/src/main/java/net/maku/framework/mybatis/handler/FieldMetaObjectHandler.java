package net.maku.framework.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import net.maku.framework.security.user.SecurityUser;
import net.maku.framework.security.user.UserDetail;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * mybatis-plus 自动填充字段
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 */
public class FieldMetaObjectHandler implements MetaObjectHandler {
    private final static String CREATE_TIME = "createTime";
    private final static String CREATOR = "creator";
    private final static String UPDATE_TIME = "updateTime";
    private final static String UPDATER = "updater";
    private final static String ORG_ID = "orgId";
    private final static String VERSION = "version";
    private final static String DELETED = "deleted";

    @Override
    public void insertFill(MetaObject metaObject) {
        UserDetail user = SecurityUser.getUser();
        LocalDateTime now = LocalDateTime.now();

        // 用户字段填充
        if (user != null) {

            /**
             * setFieldValByName方法的第三个参数metaObject是一个非常关键的对象，它代表了MyBatis中的元对象。
             * 在MyBatis中，MetaObject是用于进行反射操作的工具类，它提供了对Java Bean的属性进行读写的能力，而不需要直接使用Java的反射API。
             * 这使得在不知道具体类的情况下也能操作类的属性，增强了代码的灵活性和可维护性。
             *
             * 在FieldMetaObjectHandler的上下文中，metaObject参数指向的是当前正在处理的实体对象的MetaObject表示。
             * 当调用setFieldValByName方法时，metaObject被用来定位并设置实体对象中特定字段的值。具体来说：
             * CREATOR：这是一个字符串，代表了实体类中字段的名称，即“创建者”字段。
             * user.getId()：这是要设置的字段值，即当前登录用户的ID。
             *
             * metaObject：这是当前实体对象的MetaObject表示，setFieldValByName方法将使用它来查找并设置实体对象中名为CREATOR的字段值为user.getId()。
             * 因此，setFieldValByName(CREATOR, user.getId(), metaObject)这一行代码的作用是：在当前实体对象中，找到名为CREATOR的字段，并将其值设置为当前登录用户的ID，
             * 从而实现了自动填充创建者信息的功能。这种方式在处理大量实体类和字段时特别有用，因为它避免了在每个实体类中重复编写相同的字段填充代码。
             */

            // 创建者
            setFieldValByName(CREATOR, user.getId(), metaObject);
            // 更新者
            setFieldValByName(UPDATER, user.getId(), metaObject);
            // 创建者所属机构
            setFieldValByName(ORG_ID, user.getOrgId(), metaObject);
        }

        // 创建时间
        setFieldValByName(CREATE_TIME, now, metaObject);
        // 更新时间
        setFieldValByName(UPDATE_TIME, now, metaObject);
        // 版本号
        setFieldValByName(VERSION, 0, metaObject);
        // 删除标识
        setFieldValByName(DELETED, 0, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新者
        setFieldValByName(UPDATER, SecurityUser.getUserId(), metaObject);
        // 更新时间
        setFieldValByName(UPDATE_TIME, LocalDateTime.now(), metaObject);
    }
}