package chen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * 自定义配置文件
 * 
 * @author chenlige
 *
 */
@ConfigurationProperties(prefix = "wisely", locations = "classpath:wisely.properties")
public class WiselySettings {

    private String name;
    private String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

}