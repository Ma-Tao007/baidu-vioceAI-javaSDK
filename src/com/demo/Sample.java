package com.demo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

public class Sample {
	private static final String serverURL = "http://vop.baidu.com/server_api";
	  private static String token = "";
	  //测试文件（支持格式pcm（不压缩）、wav（不压缩，pcm编码）、amr（压缩格式））
	  private static final String testFileName = "resource/16k_10.pcm"; // 百度语音提供技术支持
	  //put your own params here
	  // 下面3个值要填写自己申请的app对应的值
	  private static final String apiKey = "awkYDhTzbTGrUAxWRfudg6Zq";//你的apikey
	  private static final String secretKey = "luSVN5nTpxKupBRnjFmABBaQia81v4qy";//你的secretKey
	  private static final String cuid = "541b:3f:5af4:b2c9";//本地mac地址，无所谓参数，为了保持唯一
	  public static void main(String[] args) throws Exception {
	  
      Long long1 = System.currentTimeMillis();
	    getToken();
	    method1();
	    method2();
	    Long long2 = System.currentTimeMillis();
	    System.out.println("耗时："+(long2-long1));
	  }
	  /**
	   * 发送get请求获取access_token
	   * @throws Exception
	   */
	  private static void getToken() throws Exception {
	    String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" +
	      "&client_id=" + apiKey + "&client_secret=" + secretKey;
	    HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL).openConnection();
	    token = new JSONObject(printResponse(conn)).getString("access_token");
	    System.out.println("使用的tonken值为："+token);
	  }
	  private static void method1() throws Exception {
	    File pcmFile = new File(testFileName);
	    HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();
	    // construct params
	    JSONObject params = new JSONObject();
	    params.put("format", "pcm");//识别的格式
	    params.put("rate", 16000);//音频格式16k或8k 采样率、16bit 位深、单声道，
	    params.put("channel", "1");//声道数，仅支持单声道，请填写固定值 1
	    params.put("token", token);//根据你的参数获取的token
	    params.put("dev_pid", 1537);//普通话识别代码
	    params.put("cuid", cuid);//用户唯一标识，用来区分用户，计算UV值。建议填写能区分用户的机器 MAC 地址或 IMEI 码，长度为60字符以内。
//	    params.put("len", pcmFile.length());//已废弃
	    //本地语音文件的的二进制语音数据 ，需要进行base64 编码。与len参数连一起使用。
	    params.put("speech", DatatypeConverter.printBase64Binary(loadFile(pcmFile)));
	    // add request header
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    // send request
	    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	    wr.writeBytes(params.toString());
	    wr.flush();
	    wr.close();
	    printResponse(conn);
	  }
	  private static void method2() throws Exception {
	    File pcmFile = new File(testFileName);
	    HttpURLConnection conn = (HttpURLConnection) new URL(serverURL
	        + "?cuid=" + cuid + "&token=" + token).openConnection();
	    // add request header
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type", "audio/pcm; rate=16000");
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    // send request
	    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	    wr.write(loadFile(pcmFile));
	    wr.flush();
	    wr.close();
	    System.out.println(getUtf8String(printResponse(conn)));
	  }
	  private static String printResponse(HttpURLConnection conn) throws Exception {
	    if (conn.getResponseCode() != 200) {
	      // request error
	     System.out.println("conn.getResponseCode() = " + conn.getResponseCode());
	      return "";
	    }
	    InputStream is = conn.getInputStream();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    String line;
	    StringBuffer response = new StringBuffer();
	    while ((line = rd.readLine()) != null) {
	      response.append(line);
	      response.append('\r');
	    }
	    rd.close();
	    System.out.println(new JSONObject(response.toString()).toString(4));
	    return response.toString();
	  }
	  private static byte[] loadFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);
	    long length = file.length();
	    byte[] bytes = new byte[(int) length];
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	        && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
	      offset += numRead;
	    }
	    if (offset < bytes.length) {
	      is.close();
	      throw new IOException("Could not completely read file " + file.getName());
	    }
	    is.close();
	    return bytes;
	  }
	  // GBK编码转为UTF-8
	  private static String getUtf8String(String s) throws UnsupportedEncodingException
	  {
	   StringBuffer sb = new StringBuffer();
	   sb.append(s);
	   String xmlString = "";
	   String xmlUtf8 = "";
		 xmlString = new String(sb.toString().getBytes("GBK"));
		 xmlUtf8 = URLEncoder.encode(xmlString , "GBK");
	   return URLDecoder.decode(xmlUtf8, "UTF-8");
	  }
}
