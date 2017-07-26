package chen;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import chen.config.WiselySettings;
import chen.servlet.MyServlet1;

@SpringBootApplication //等价于以默认属性使用 @Configuration,@EnableAutoConfiguration 和 @ComponentScan 
@ServletComponentScan // 这个就是扫描相应的Servlet 注解;
@EnableTransactionManagement // 开启注解事务管理，等同于xml配置文件中的 <tx:annotation-driven />
@EnableConfigurationProperties({WiselySettings.class})//自定义配置文件properties
@ComponentScan(basePackages={"cn.kfit","org.kfit","chen"})//配置扫描包，可以不加，默认此类的同级或子包
@EnableCaching//开启缓存功能
public class BeautyApplication extends SpringBootServletInitializer
// implements TransactionManagementConfigurer
{

    public static void main(String[] args) {
//         SpringApplication application = new SpringApplication(BeautyApplication.class);
        /*
         * Banner.Mode.OFF:关闭; Banner.Mode.CONSOLE:控制台输出，默认方式;
         * Banner.Mode.LOG:日志输出方式;
//         */
//         application.setBannerMode(Banner.Mode.OFF);
//         application.run(args);
        SpringApplication.run(BeautyApplication.class, args);
    }
    /*
     * 
     * 支持tomcat启动
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BeautyApplication.class);
    }
    
    /**
     * 配置文件上傳限制
     * @return
     */
    @Bean 
    public MultipartConfigElement multipartConfigElement() { 
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //// 设置文件大小限制 ,超了，页面会抛出异常信息，这时候就需要进行异常信息的处理了;
        factory.setMaxFileSize("128KB"); //KB,MB
        /// 设置总上传数据总大小
        factory.setMaxRequestSize("256KB"); 
        //Sets the directory location where files will be stored.
        //factory.setLocation("路径地址");
        return factory.createMultipartConfig(); 
    } 
    
    /**
     * 注册Servlet.不需要添加注解：@ServletComponentScan
     * @return
     */
    @Bean
    public ServletRegistrationBean MyServlet1(){
         return new ServletRegistrationBean(new MyServlet1(),"/myServlet/*");
    }
    
 // @Bean
    // public Object testBean(PlatformTransactionManager
    // platformTransactionManager) {
    // System.out.println(">>>>>>>>>>" +
    // platformTransactionManager.getClass().getName());
    // return new Object();
    // }
  //添加依赖包后就不要配置事务管理器,自动配置事务管理器
//    @Resource(name = "txManager2")
//    private PlatformTransactionManager txManager2;
//
//    // 创建事务管理器1
//
//    @Bean(name = "txManager1")
//    public PlatformTransactionManager txManager(DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }
//
//    // 创建事务管理器2
//    @Bean(name = "txManager2")
//    public PlatformTransactionManager txManager2(EntityManagerFactory factory) {
//        System.out.println("======================");
//        return new JpaTransactionManager(factory);
//    }
//
//    // 实现接口 TransactionManagementConfigurer 方法，其返回值代表在拥有多个事务管理器的情况下默认使用的事务管理器
//    @Override
//    public PlatformTransactionManager annotationDrivenTransactionManager() {
//        return txManager2;
//    }

}