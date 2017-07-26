package chen.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chen.domain.Person;
import chen.service.PersonService;

@RestController
@RequestMapping("/person")
public class PersonController {

    protected static Logger logger = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    private PersonService personService;

    /**
     * 测试person保存
     * 
     * @return
     * @throws Exception 
     */
    @RequestMapping("/savePerson")
    public String savePerson() throws Exception {
        Person p = new Person();
        p.setId((long) 4);
        p.setName("chen");
        p.setAge(28);
        p.setAddress("aaaa");
        personService.save(p);

        return "success";
    }

    @RequestMapping("/find/name/{name}/age/{age}")
    public List<Person> find(HttpServletRequest request, @PathVariable("name") String name, @PathVariable("age") Integer age) {

        logger.info("查询条件：名字{}，年龄{}",name,age);
        
        return personService.find(name,age);
    }
    
    @RequestMapping("/find/department/{id}")
    public List<Person> findByDepartment_IdAndName(HttpServletRequest request,@PathVariable("id") Long id) {

        logger.info("查询条件：部门id{}",id);
        
        return personService.findByDepartmentId(id);
    }
}
