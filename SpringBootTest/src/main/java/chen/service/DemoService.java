package chen.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.nutz.http.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.com.infcn.core.HttpApiClient;
import cn.com.infcn.core.pageModel.Demo;
import cn.com.infcn.core.util.CommonUtil;
import cn.com.infcn.core.util.StringUtil;
import cn.com.infcn.demo.model.NixiaMsp;
import cn.com.infcn.demo.model.PageModel;
import cn.com.infcn.demo.model.UploadFile;

@Service
public class DemoService {

	/** mss服务器IP */
	@Value("${mssIp}")
	private String mssIp;
	/** mss服务器端口 */
	@Value("${mssPort}")
	private int mssPort;

	public JSONObject getResult() {

		try {

			HttpApiClient api = CommonUtil.getMspApi(mssIp, mssPort);
			Map<String, Object> params = new HashMap<>();
			params.put("field", "doc_mean");

			Response resp = api.post("MssSearchApi", "aggregationsByQuery", Integer.MAX_VALUE, params);

			if (resp != null) {

				return JSONObject.parseObject(resp.getContent());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 首页分类信息
	 * 
	 * @return
	 */
	public Map<String, String> getIndexInfo(String type) {
		Map<String, Object> params = new HashMap<>();
		return agg("type_source", params, type);

	}

	/**
	 * 获取分面结果
	 * 
	 * @param file
	 * @param doc_type
	 * @return
	 * @return
	 */
	public Map<String, Object> getFacetInfo(String file, Map<String, String> mapLsit, String keyword) {

		Map<String, Object> map = new HashMap<>();

		for (Entry<String, String> entry : mapLsit.entrySet()) {
			StringBuffer sb = new StringBuffer();
			Map<String, Object> params = new HashMap<>();
			sb.append("(  ");
			String type_source = entry.getKey();
			sb.append(" type_source :" + type_source);
			if (StringUtils.isNotEmpty(keyword)) {
				if (!"all".equals(file)) {
					if ("title".equals(file)) {
						sb.append(" AND (title: " + keyword + " OR title_h2:" + keyword + " OR title_h3:" + keyword
								+ " OR title_h4:" + keyword + ")");

					} else {
						sb.append(" AND " + file + ":" + keyword + "");
					}
				} else {
					sb.append(" AND (title:*" + keyword + "* OR content:*" + keyword + "* OR author:" + keyword
							+ " OR title_h2:" + keyword + " OR title_h3:" + keyword + " OR title_h4:" + keyword
							+ " OR Summary:*" + keyword + "*)");

				}
			}
			sb.append(")");
			params.put("query", sb.toString());
			map.put(type_source, this.agg("type_category", params, ""));
		}

		return map;
	}

	private Map<String, String> agg(String file, Map<String, Object> params, String type) {

		List<Map<String, String>> resList = new ArrayList<>();
		// if(StringUtils.isNotEmpty(type)){
		// Map<String, String> newmap=new HashMap<>();
		// newmap.put("key", "全部");
		// resList.add(newmap);
		// }
		Map<String, String> map = new HashMap<>();
		try {

			HttpApiClient api = CommonUtil.getMspApi(mssIp, mssPort);
			params.put("field", file);
			params.put("size", Integer.MAX_VALUE);
			params.put("tableNames", "patent_ipc,patent_loc,gjdw,national_standards");
			String url = "http://192.168.10.75:9095/api/MssSearchApi/aggregationsByQuery?";

			for (String key : params.keySet()) {

				url += key + "=" + params.get(key) + "&";

			}
			System.out.println(url.substring(0, url.length() - 1));
			Response resp = api.post("MssSearchApi", "aggregationsByQuery", Integer.MAX_VALUE, params);

			if (resp != null) {

				JSONObject json = JSONObject.parseObject(resp.getContent());
				JSONObject obj = json.getJSONObject("obj");
				if (obj != null && !obj.isEmpty()) {

					Object[] objArr = obj.getJSONObject("aggregations").getJSONObject(file).getJSONArray("buckets")
							.toArray();
					for (Object o : objArr) {

						JSONObject j = JSONObject.parseObject(o.toString());
						if (StringUtils.isNotEmpty(j.getString("key"))) {
							map.put(j.getString("key"), j.getString("doc_count"));
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getResultList(Demo demo) {

		Map<String, Object> resMap = new HashMap<>();

		Map<String, Object> params = new HashMap<>();
		params.put("tableNames", "gjdw,national_standards,patent_ipc,patent_loc");
		params.put("from", demo.getPageStart());
		params.put("size", demo.getPage());

		// 设置查询字段信息
		String[] words = null;
		if ("all".equals(demo.getSearchWord())) {
			words = new String[] { "title", "Summary", "author" };
		} else if ("title".equals(demo.getSearchWord())) {
			words = new String[] { "title" };
		} else if ("author".equals(demo.getSearchWord())) {
			words = new String[] { "author" };
		} else if ("Summary".equals(demo.getSearchWord())) {
			words = new String[] { "Summary" };
		}

		// 设置高亮字段
		if (StringUtils.isNotEmpty(demo.getKeyword())) {
			params.put("heighlight", getHeighlightStr(words, demo.getKeyword()));
		}
		// 设置查询语句
		StringBuffer sb = new StringBuffer();
		sb.append("( ");
		sb.append(" *:* ");
		if (StringUtils.isNotEmpty(demo.getKeyword())) {
			sb.append(" AND " + this.getQuery(words, demo.getKeyword()));
		}
		if (StringUtils.isNotEmpty(demo.getFacetWord())) {
			sb.append(" AND " + demo.getFacetWord() + ":\"" + demo.getFacetVal() + "\"");
		}
		if (StringUtils.isNotEmpty(demo.getDocType()) && !"全部".equals(demo.getDocType())) {
			sb.append(" AND type_source:\"" + demo.getDocType() + "\"");
		}
		sb.append(" )");
		params.put("query", sb.toString());
		// 设置排序
		// params.put("sort", "release_time|" + demo.getSort());

		String url = "http://192.168.3.186:9095/MssSearchApi/searchByQuery?";

		for (String key : params.keySet()) {

			url += key + "=" + params.get(key) + "&";

		}
		System.out.println(url.substring(0, url.length() - 1));

		try {
			HttpApiClient api = CommonUtil.getMspApi(mssIp, mssPort);

			Response resp = api.post("MssSearchApi", "searchByQuery", Integer.MAX_VALUE, params);

			if (resp != null) {

				JSONObject jsonObj = JSONObject.parseObject(resp.getContent());
				System.out.println(jsonObj);
				JSONObject jsonObject = jsonObj.getJSONObject("obj").getJSONObject("hits");
				List<JSONObject> resList = new ArrayList<>();
				for (Object obj : jsonObject.getJSONArray("hits").toArray()) {
					String id = JSONObject.parseObject(obj.toString()).getString("_id");
					JSONObject j = JSONObject.parseObject(obj.toString()).getJSONObject("_source");
					// J.put("", value);
					String keyWord = getKeyWord(j.getString("title") + "," + j.getString("title_h2") + ","
							+ j.getString("title_h3") + "," + j.getString("title_h4") + "," + j.getString("content"));
					if (StringUtils.isNotEmpty(j.getString("information_source"))) {
						j.put("information_source", StringUtil.delHTMLTag(j.getString("information_source")));
					}
					String content = j.getString("Summary");
					if(content != null){
						if (content.length() > 200) {
							Integer start = content.indexOf(demo.getKeyword()) < 100 ? 0
									: content.indexOf(demo.getKeyword()) - 100;
							Integer end = content.length() < content.indexOf(demo.getKeyword()) + 100 ? content.length()
									: content.indexOf(demo.getKeyword()) + 100;
							content = content.substring(start, end);
						}
						if (content != null && content.contains("<em>"))
							j.put("Summary_hl", content);
						else if (content != null && !content.contains("<em>")) {
							j.put("Summary_hl",
									content.replace(demo.getKeyword(), "<em>" + demo.getKeyword() + "</em>"));
						}
					}

					j.put("keyWord", keyWord);
					j.put("_id", id);
					resList.add(j);
				}
				// 返回结果
				resMap.put("list", resList);
				// 设置命中条数
				resMap.put("count", jsonObject.getInteger("total"));

				// 配置分页信息
				int count = jsonObject.getInteger("total").intValue();
				PageModel<JSONObject> pm = new PageModel<>();
				pm.setPage(Integer.valueOf(demo.getNowPage()));// 设置当前页
				pm.init((List) resMap.get("list"), demo.getPage(), Integer.valueOf(String.valueOf(count)).intValue());
				resMap.put("page", pm.displayForPage("adSearch"));

				return resMap;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	// 设置高亮字段
	private String getHeighlightStr(String[] words, String keyword) {
		keyword = keyword.trim();
		JSONArray arr = new JSONArray();
		for (String word : words) {
			JSONObject job = new JSONObject();
			job.put("query", keyword);
			job.put("begin", "<em>");
			job.put("end", "</em>");
			job.put("single", true);
			JSONObject titfield = new JSONObject();
			titfield.put("field", word);
			titfield.put("dist_field", word + "_hl");
			titfield.put("size", 200);
			job.put("fields", new Object[] { titfield });
			arr.add(job);
		}
		return arr.toJSONString();
	}

	// 获取检索条件
	private String getQuery(String[] queryArr, String keyword) {

		StringBuffer sb = new StringBuffer("(");
		for (int i = 0; i < queryArr.length; i++) {
			// if (i != queryArr.length - 1) {
			// sb.append(queryArr[i] + ":\"" + keyword.trim() + "\"^100 OR " +
			// queryArr[i] + ":" + keyword.trim()
			// + "^50 OR " + queryArr[i] + ":*" + keyword.trim() + "* OR ");
			// } else {
			// sb.append(queryArr[i] + ":\"" + keyword.trim() + "\"^100 OR " +
			// queryArr[i] + ":" + keyword.trim()
			// + "^50" + queryArr[i] + ":*" + keyword.trim() + "*");
			// }
			if (i != queryArr.length - 1) {
				sb.append(queryArr[i] + ":*" + keyword.trim() + "* OR ");
			} else {
				sb.append(queryArr[i] + ":*" + keyword.trim() + "*");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public JSONObject getInfo(String id, String typeSource, String typeCategory) {
		JSONObject result = null;
		Map<String, Object> params = new HashMap<>();
		try {

			HttpApiClient api = CommonUtil.getMspApi(mssIp, mssPort);
			params.put("id", id);
			if ("国家电网（公司资讯）".equals(typeSource) || "南瑞集团（新闻中心）".equals(typeSource)) {
				params.put("tableName", "gjdw");
			} else if ("国家标准".equals(typeSource)) {
				params.put("tableName", "national_standards");
			} else if ("国家专利".equals(typeSource) && "IPC分类".equals(typeCategory)) {
				params.put("tableName", "patent_ipc");
			} else if ("国家专利".equals(typeSource) && "LOC分类".equals(typeCategory)) {
				params.put("tableName", "patent_loc");
			}
			Response resp = api.post("MssSearchApi", "findById", Integer.MAX_VALUE, params);

			if (resp != null) {

				JSONObject json = JSONObject.parseObject(resp.getContent());
				JSONObject obj = json.getJSONObject("obj");
				if (obj != null && !obj.isEmpty()) {

					String keyWord = getKeyWord(
							obj.getString("title") + "," + obj.getString("title_h2") + "," + obj.getString("title_h3")
									+ "," + obj.getString("title_h4") + "," + obj.getString("content"));
					obj.put("keyWord", keyWord);
					result = obj;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 相关公文查询
	 * 
	 * @param demo
	 * @return
	 */
	public List<JSONObject> relevanceSearch(Demo demo) {

		Map<String, Object> params = new HashMap<>();
		params.put("tableNames", "gjdw,national_standards,patent_ipc,patent_loc");
		params.put("size", 10);

		String[] words = demo.getWords();
		params.put("query", this.getQuery(words, demo.getKeyword()));

		try {
			HttpApiClient api = CommonUtil.getMspApi(mssIp, mssPort);
			Response resp = api.post("MssSearchApi", "searchByQuery", Integer.MAX_VALUE, params);

			if (resp != null) {

				JSONObject jsonObj = JSONObject.parseObject(resp.getContent());
				JSONObject jsonObject = jsonObj.getJSONObject("obj").getJSONObject("hits");
				List<JSONObject> resList = new ArrayList<>();
				for (Object obj : jsonObject.getJSONArray("hits").toArray()) {
					resList.add(JSONObject.parseObject(obj.toString()));
				}

				return resList;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<JSONObject> relevanceAuthor(String typeCategry, String typeSource) {
		Map<String, Object> params = new HashMap<>();
		params.put("tableNames", "gjdw,national_standards,patent_ipc,patent_loc");
		params.put("size", 10);
		StringBuffer sb = new StringBuffer();
		sb.append("( *:*");
		if (StringUtils.isNotEmpty(typeCategry)) {
			sb.append(" AND type_category:\"" + typeCategry + "\"");

		}
		if (StringUtils.isNotEmpty(typeSource)) {
			sb.append(" AND  type_source:\"" + typeSource + "\"");
		}
		sb.append(")");
		params.put("query", sb.toString());
		params.put("sort", "release_time|desc");
		try {
			HttpApiClient api = CommonUtil.getMspApi(mssIp, mssPort);
			Response resp = api.post("MssSearchApi", "searchByQuery", Integer.MAX_VALUE, params);

			if (resp != null) {

				JSONObject jsonObj = JSONObject.parseObject(resp.getContent());
				JSONObject jsonObject = jsonObj.getJSONObject("obj").getJSONObject("hits");
				List<JSONObject> resList = new ArrayList<>();
				for (Object obj : jsonObject.getJSONArray("hits").toArray()) {
					resList.add(JSONObject.parseObject(obj.toString()));
				}

				return resList;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查询相关关键字
	 * 
	 * @param demo
	 * @return
	 */
	public List<String> relevanceKeyword(Demo demo) {

		Set<String> set = new HashSet<>();

		List<JSONObject> list = this.relevanceSearch(demo);
		for (JSONObject obj : list) {

			JSONObject json = obj.getJSONObject("_source");
			if (json != null) {
				String[] arr = json.get("keyword").toString().split("、");
				for (String keyword : arr) {
					set.add(keyword);
				}
			}
		}

		List<String> resList = new ArrayList<>();
		resList.addAll(set);

		return resList;
	}

	public List<JSONObject> relevanceBranch(Demo demo) {
		return this.relevanceSearch(demo);
	}

	public void insertMsp(Object o) {

		HttpApiClient client = new HttpApiClient(mssIp, mssPort);
		Map<String, Object> params = new HashMap<>();
		params.put("name", "gjdw,national_standards,patent_ipc,patent_loc");
		params.put("jsonStr", JSONObject.toJSONString(o));
		params.put("skiperr", true);
		Response rsp;
		try {
			rsp = client.post("MssDataApi", "insert", Integer.MAX_VALUE, params);

			if (rsp != null) {
				String jsonStr = rsp.getContent();
				JSONObject jsonObj = JSONObject.parseObject(jsonStr);
				JSONObject obj = jsonObj.getJSONObject("obj");
				if (!jsonObj.getBooleanValue("ok")) {
					if (!obj.getJSONArray("errors").isEmpty()) {
						// result.setErrors(obj.getJSONArray("errors").toArray());
					}

				}
				// result.setOk(jsonObj.getBooleanValue("ok"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void analysisFile(UploadFile uploadFile, NixiaMsp msp, String path) {
		File f = new File(path + uploadFile.getFilePath());
		String full_text = "";
		try {
			if (uploadFile.getFileSuffix().equals("doc")) {
				FileInputStream fis = new FileInputStream(f);
				WordExtractor ex = new WordExtractor(fis);
				full_text = ex.getText();

			} else if (uploadFile.getFileSuffix().equals("docx")) {
				OPCPackage opcPackage = POIXMLDocument.openPackage(path + uploadFile.getFilePath());
				POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
				full_text = extractor.getText();
				System.out.println(full_text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		msp.setFile_full_text(full_text);
		msp.setFile_name(uploadFile.getFileName());
		msp.setDoc_name(uploadFile.getFileName());
		msp.setFile_path(uploadFile.getFilePath());
		String type = uploadFile.getFileName().substring(uploadFile.getFileName().length() - 2,
				uploadFile.getFileName().length());
		if ("通知".equals(type)) {
			msp.setDoc_type("通知");
		} else if ("意见".equals(type)) {
			msp.setDoc_type("意见");
		} else if ("批复".equals(type)) {
			msp.setDoc_type("批复");
		} else {
			msp.setDoc_type("其他");
		}
		boolean isEixct = false;
		List<String> s = new ArrayList<String>();
		if (full_text.indexOf("综合政务") > 0) {
			s.add("综合政务");
			isEixct = true;
		}
		if (full_text.indexOf("财务") > 0 || full_text.indexOf("金融") > 0 || full_text.indexOf("审计") > 0) {
			s.add("财务、金融、审计");
			isEixct = true;
		}
		if (full_text.indexOf("农业") > 0 || full_text.indexOf("林业") > 0 || full_text.indexOf("水利") > 0) {
			s.add("农业、林业、水利");
			isEixct = true;
		}
		if (full_text.indexOf("国土资源") > 0 || full_text.indexOf("能源") > 0) {
			s.add("国土资源、能源");
			isEixct = true;
		}
		if (full_text.indexOf("公安") > 0 || full_text.indexOf("安全") > 0 || full_text.indexOf("司法") > 0) {
			s.add("公安、安全、司法");
			isEixct = true;
		}
		if (full_text.indexOf("民政") > 0 || full_text.indexOf("扶贫") > 0 || full_text.indexOf("救灾") > 0) {
			s.add("民政、扶贫、救灾");
			isEixct = true;
		}
		if (full_text.indexOf("民族") > 0 || full_text.indexOf("宗教") > 0) {
			s.add("民族、宗教");
			isEixct = true;
		}
		if (full_text.indexOf("工业") > 0 || full_text.indexOf("交通") > 0) {
			s.add("工业、交通");
			isEixct = true;
		}
		if (full_text.indexOf("科技") > 0 || full_text.indexOf("教育") > 0) {
			s.add("科技、教育");
			isEixct = true;
		}
		if (!isEixct) {
			s.add("其他");
		}
		msp.setDoc_mean(s.toArray(new String[s.size()]));
		String keyword = getKeyWord(full_text);
		msp.setKeyword(keyword);

		msp.setLevel("普通");
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		msp.setPublish_date(sf.format(new Date()));
		// insertMsp(msp);
	}

	private String getKeyWord(String full_text) {

		String keyWords = "";
		try {
			Map<String, Object> params = new HashMap<>();
			HttpApiClient api = CommonUtil.getMspApi(mssIp, mssPort);
			params.put("content", full_text);
			params.put("size", 5);
			params.put("tableNames", "gjdw,national_standards,patent_ipc,patent_loc");

			Response resp = api.post("MssNlpApi", "keyword", Integer.MAX_VALUE, params);

			if (resp != null) {

				JSONObject json = JSONObject.parseObject(resp.getContent());
				JSONObject obj = json.getJSONObject("obj");
				if (obj != null && !obj.isEmpty()) {

					Object[] objArr = obj.getJSONArray("keywords").toArray();
					for (Object o : objArr) {

						JSONObject j = JSONObject.parseObject(o.toString());

						if (keyWords == null || keyWords == "") {
							keyWords = j.getString("name");
						} else {
							keyWords = keyWords + "、" + j.getString("name");
						}

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return keyWords;
	}

}
