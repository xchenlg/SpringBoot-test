package chen.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/thymeleaf")
public class TestThymeleafController {

    protected static Logger logger = LoggerFactory.getLogger(TestThymeleafController.class);

    @RequestMapping("/helloHtml")
    public String hello(Map<String,Object> map) {
        map.put("hello","from TestThymeleafController.helloHtml");
        return "/helloHtml";
    }

}
