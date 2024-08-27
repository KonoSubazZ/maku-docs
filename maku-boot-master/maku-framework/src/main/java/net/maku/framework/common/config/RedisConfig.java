package net.maku.framework.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis配置
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 */
@Configuration
public class RedisConfig {

    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {

        // 创建了一个 ObjectMapper 实例，它是 Jackson 库的核心类，用于将 Java 对象序列化为 JSON 字符串，
        // 或将 JSON 字符串反序列化为 Java 对象。
        ObjectMapper objectMapper = new ObjectMapper();

        // 禁止将日期格式化为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 这一行代码注册了一个 JavaTimeModule 模块。JavaTimeModule 是 Jackson 提供的一个模块，用于支持 Java 8 的 java.time
        // 包中的日期和时间类型（如 LocalDate, LocalDateTime 等）。通过注册这个模块，可以更好地处理这些日期时间类型的数据。
        objectMapper.registerModule(new JavaTimeModule());


        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // Key HashKey使用String序列化
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        
        // Value HashValue使用Json序列化
        template.setValueSerializer(genericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer());

        template.setConnectionFactory(factory);

        template.afterPropertiesSet();
        return template;
    }
}
