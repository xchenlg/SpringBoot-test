package chen.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.sf.json.JSONObject;

/**
 *  测试jsonp
 */
@Controller
@RequestMapping("/jsonp")
public class JsonpController {

    protected static Logger logger = LoggerFactory.getLogger(JsonpController.class);

    @RequestMapping(value = "/save", method = RequestMethod.GET)
    public void save(String name, String address, Integer age) {
        logger.debug("save 开始");
        logger.debug("save 结束");
    }

    @RequestMapping("/testjsp")
    public void exchangeJson(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/plain");
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            Map<String, String> map = new HashMap<String, String>();
            map.put("result", "content");
            PrintWriter out = response.getWriter();
            JSONObject resultJSON = JSONObject.fromObject(map); // 根据需要拼装json
            String jsonpCallback = request.getParameter("jsonpCallback");// 客户端请求参数
            out.println(jsonpCallback + "(" + resultJSON.toString(1, 1) + ")");// 返回jsonp格式数据
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //
    // @RequestMapping(value = "/q2", method = RequestMethod.GET)
    // public List<Person> q2(String name, String address) {
    // logger.debug("q2 开始");
    // logger.debug("q2接收参数name={},address={}", name, address);
    // return personRepository.findByNameAndAddress(name, address);
    // }
    //
    // @RequestMapping(value = "/q3", method = RequestMethod.GET)
    // public List<Person> q3(String name, String address) {
    // logger.debug("q3 开始");
    // logger.debug("q3接收参数name={},address={}", name, address);
    // return personRepository.withNameAndAddressQuery(name, address);
    // }

}
