package chen.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

import cn.com.infcn.ade.core.controller.BaseController;
import cn.com.infcn.core.pageModel.Demo;
import cn.com.infcn.core.util.BaseUploadFiles;
import cn.com.infcn.demo.model.NixiaMsp;
import cn.com.infcn.demo.model.UploadFile;
import cn.com.infcn.demo.service.DemoService;

/**
 * 宁夏DEMO项目的控制类
 * 
 * @author yangyang
 */
@Controller
@RequestMapping("/web/demoController")
public class DemoController   {

	@SuppressWarnings("serial")
	public static final Map<String, Integer> m = new HashMap<String, Integer>() {
		{
			put("国家电网（公司资讯）", 1);
			put("南瑞集团（新闻中心）", 2);
			put("国家标准", 3);
			put("国家专利", 4);
		}
	};

	@Autowired
	private DemoService demoService;
	private int pageSize = 6;

	@RequestMapping("/index2")
	public String index(HttpServletRequest rq) {

		return "/demo/ningxia/result";

	}

	@RequestMapping("/searchPage")
	public String searchPage(HttpServletRequest rq) {

		rq.setAttribute("list", this.demoService.getIndexInfo("doc_type"));

		return "/demo/ningxia/searchPage";

	}

	@RequestMapping("/uploadPage")
	public String uploadPage(HttpServletRequest rq) {

		return "/demo/ningxia/uploadPage";

	}

	@RequestMapping("/index")
	public String resultPage(HttpServletRequest rq) {

		Map<String, String> mapLsit = this.demoService.getIndexInfo("type_source");

		// 检索框信息
		rq.setAttribute("list", mapLsit);

		String file = rq.getParameter("file");
		String type_source = rq.getParameter("type_source");
		String keyword = rq.getParameter("keyword");
		if (StringUtils.isEmpty(file)) {
			file = "all";
		}

		rq.setAttribute("keyword", keyword);
		rq.setAttribute("file", file);
		rq.setAttribute("type_source", type_source);

		// 获取分面信息
		Map<String, Object> map = this.demoService.getFacetInfo(file, mapLsit, keyword);

		// 强制排序
		TreeMap<String, Object> tree = new TreeMap<String, Object>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (m.get(o1) == null || m.get(o2) == null) {
					return 1;
				}
				return m.get(o1).compareTo(m.get(o2));
			}
		});

		tree.putAll(map);
		rq.setAttribute("type_sourceList", tree);

		return "/demo/ningxia/result";

	}

	@RequestMapping("/getResult")
	@ResponseBody
	public JSONObject getResult() {
		return this.demoService.getResult();
	}

	/**
	 * 获取订单列表
	 * 
	 * @return 页面路径
	 */
	@RequestMapping("/getResultList")
	public String getResultList(HttpServletRequest rq, Demo demo) {

		// 获取查询结果
		demo.setPage(pageSize);// 列表显示条数
		demo.setNowPage(demo.getPageStart());// 显示当前页数
		demo.setPageStart((demo.getPageStart() - 1) * pageSize);// 检索开始条数
		Map<String, Object> resMap = this.demoService.getResultList(demo);
		rq.setAttribute("resMap", resMap.get("list"));
		rq.setAttribute("page", resMap.get("page"));
		rq.setAttribute("searchWord", demo.getSearchWord());
		rq.setAttribute("keyWord", demo.getKeyword());
		rq.setAttribute("demo", demo);

		return "/demo/ningxia/result_list";
	}

	/**
	 * 列表分面页
	 * 
	 * @return
	 */
	@RequestMapping("/initFacet")
	public String initFacet(HttpServletRequest rq, Demo demo) {
		Map<String, String> mapLsit = this.demoService.getIndexInfo("type_source");
		Map<String, Object> map = this.demoService.getFacetInfo(demo.getSearchWord(), mapLsit, demo.getKeyword());

		// 强制排序
		TreeMap<String, Object> tree = new TreeMap<String, Object>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (m.get(o1) == null || m.get(o2) == null) {
					return 1;
				}
				return m.get(o1).compareTo(m.get(o2));
			}
		});

		tree.putAll(map);
		rq.setAttribute("type_sourceList", tree);

		return "/demo/ningxia/facetPage";
	}

	/**
	 * 获取详情
	 */
	@RequestMapping("/resultInfo")
	public String resultInfo(HttpServletRequest rq, String typeSource, String typeCategory) {
		// 检索框信息
		// rq.setAttribute("list", this.demoService.getIndexInfo("doc_type"));
		String id = rq.getParameter("id");
		// 获取详情信息
		JSONObject result = this.demoService.getInfo(id, typeSource, typeCategory);
		rq.setAttribute("item", result);
		rq.setAttribute("id", id);

		rq.setAttribute("docType", rq.getParameter("docType"));
		rq.setAttribute("searchWord", rq.getParameter("searchWord"));

		System.out.println(rq.getParameter("docType") + "  " + rq.getParameter("searchWord"));

		// 设置关键字
		if (result != null && StringUtils.isNotEmpty(result.getString("keyWord"))) {
			rq.setAttribute("keywords", result.getString("keyWord").split("、"));
		}

		return "/demo/ningxia/pages_view";
	}

	@RequestMapping("/relevanceSearch")
	@ResponseBody
	public List<JSONObject> relevanceSearch(HttpServletRequest rq, Demo demo) {

		String[] words = new String[] { "doc_name", "file_name", "file_full_text" };
		demo.setWords(words);

		return this.demoService.relevanceSearch(demo);
	}

	/**
	 * 下载文件
	 */
	@RequestMapping("/downFile")
	public void downFile(HttpServletRequest request, HttpServletResponse response) {
		String id = request.getParameter("id");
		// 获取详情信息
		// TODO
		JSONObject result = this.demoService.getInfo(id, null, null);
		String fileName = result.getString("file_name");
		String filePath = result.getString("file_path");
		filePath = request.getSession().getServletContext().getRealPath(filePath);
		File f = new File(filePath);
		FileInputStream fin = null;
		OutputStream os = null;
		try {
			fin = new FileInputStream(f);
			os = response.getOutputStream();
			byte[] buf = new byte[1024];
			int r = 0;
			response.setContentType("application/msword;charset=uff-8");
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(fileName.getBytes(), "iso-8859-1"));
			while ((r = fin.read(buf, 0, buf.length)) != -1) {
				os.write(buf, 0, r);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fin != null) {
				try {
					fin.close();
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	@RequestMapping("/relevanceAuthor")
	@ResponseBody
	public List<JSONObject> relevanceAuthor(HttpServletRequest rq, String typeCategry, String typeSource) {

		return this.demoService.relevanceAuthor(typeCategry, typeSource);

	}

	@RequestMapping("/relevanceKeyword")
	@ResponseBody
	public List<String> relevanceKeyword(HttpServletRequest rq, Demo demo) {

		String[] words = new String[] { "keyword" };
		demo.setWords(words);
		return this.demoService.relevanceKeyword(demo);

	}

	@RequestMapping("/relevanceBranch")
	@ResponseBody
	public List<JSONObject> relevanceBranch(HttpServletRequest rq, Demo demo) {

		String[] words = new String[] { "publish_branch" };
		demo.setWords(words);

		return this.demoService.relevanceBranch(demo);
	}

	@ResponseBody
	@RequestMapping("/batchUpdateRiskRule")
	public String batchUpdateRiskRule(@RequestParam("updateFiles") MultipartFile[] updateFiles,
			HttpServletRequest request) throws IOException {

		return null;

	}

	@RequestMapping(value = "/uploadFiles", method = RequestMethod.POST)
	public void uploadFiles(@RequestParam("file") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) {
		try {

			UploadFile uploadFile = new UploadFile();
			BaseUploadFiles.upload(file, "uploadFile", request, uploadFile);
			NixiaMsp msp = new NixiaMsp();
			this.demoService.analysisFile(uploadFile, msp, request.getSession().getServletContext().getRealPath("/"));
			this.demoService.insertMsp(msp);
			response.getWriter().print(uploadFile.getFileName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
