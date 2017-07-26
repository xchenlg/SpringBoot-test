package test;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import chen.BeautyApplication;
import chen.domain.Person;
import chen.service.PersonService;

//// SpringJUnit支持，由此引入Spring-Test框架支持！
@RunWith(SpringJUnit4ClassRunner.class)
//// 指定我们SpringBoot工程的Application启动类
@SpringApplicationConfiguration(classes = BeautyApplication.class)
/// 由于是Web项目，Junit需要模拟ServletContext，因此我们需要给我们的测试类加上@WebAppConfiguration。
@WebAppConfiguration
public class HelloServiceTest {

    @Resource
    private PersonService personService;

    @Test
    public void testGetName() {
        System.out.println(personService.findByCourse_Name("5"));
    }
    
    @Test
    public void findByCoureseId() {
        List<Person> p = personService.findByCoureseId((long) 5);
        System.out.println(p.get(0).getCourses().size());
    }
    
    
}