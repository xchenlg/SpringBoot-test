package chen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chen.domain.Person;
import chen.service.PersonService;

@RestController
@RequestMapping("/hello")
public class HelloController {

    protected static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private PersonService personService;

    @RequestMapping("/test")
    public String hello() {
        logger.info("测试。。。");
        return "Hello world!";
    }

    /**
     * 测试异常 统一异常处理
     * 
     * @return
     */
    @RequestMapping("/zeroException")
    public int zeroException() {
        return 100 / 0;
    }

    /**
     * 测试person保存
     * 
     * @return
     * @throws Exception 
     */
    @RequestMapping("/savePerson")
    public String savePerson() throws Exception {
        Person p = new Person();
        p.setId((long) 1);
        p.setName("chen");
        p.setAge(28);
        personService.save(p);

        return "success";
    }
}
