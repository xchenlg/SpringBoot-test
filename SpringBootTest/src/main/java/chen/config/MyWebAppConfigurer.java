package chen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import chen.servlet.MyInterceptor1;
import chen.servlet.MyInterceptor2;

@Configuration
public class MyWebAppConfigurer extends WebMvcConfigurerAdapter {

    /* 
     * 
     * 配置静态资源文件
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        //location可以配置多个
        registry.addResourceHandler("/myres/**").addResourceLocations("classpath:/myres1/").addResourceLocations("classpath:/myres/");
        //可以进行本地文件配置
        registry.addResourceHandler("/api_files/**").addResourceLocations("file:D:/data/api_files");
        super.addResourceHandlers(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        registry.addInterceptor(new MyInterceptor1()).addPathPatterns("/**");
        registry.addInterceptor(new MyInterceptor2()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }

}