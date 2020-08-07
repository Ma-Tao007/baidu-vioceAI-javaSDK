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
	  //�����ļ���֧�ָ�ʽpcm����ѹ������wav����ѹ����pcm���룩��amr��ѹ����ʽ����
	  private static final String testFileName = "resource/16k_10.pcm"; // �ٶ������ṩ����֧��
	  //put your own params here
	  // ����3��ֵҪ��д�Լ������app��Ӧ��ֵ
	  private static final String apiKey = "awkYDhTzbTGrUAxWRfudg6Zq";//���apikey
	  private static final String secretKey = "luSVN5nTpxKupBRnjFmABBaQia81v4qy";//���secretKey
	  private static final String cuid = "541b:3f:5af4:b2c9";//����mac��ַ������ν������Ϊ�˱���Ψһ
	  public static void main(String[] args) throws Exception {
	  
      Long long1 = System.currentTimeMillis();
	    getToken();
	    method1();
	    method2();
	    Long long2 = System.currentTimeMillis();
	    System.out.println("��ʱ��"+(long2-long1));
	  }
	  /**
	   * ����get�����ȡaccess_token
	   * @throws Exception
	   */
	  private static void getToken() throws Exception {
	    String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" +
	      "&client_id=" + apiKey + "&client_secret=" + secretKey;
	    HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL).openConnection();
	    token = new JSONObject(printResponse(conn)).getString("access_token");
	    System.out.println("ʹ�õ�tonkenֵΪ��"+token);
	  }
	  private static void method1() throws Exception {
	    File pcmFile = new File(testFileName);
	    HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();
	    // construct params
	    JSONObject params = new JSONObject();
	    params.put("format", "pcm");//ʶ��ĸ�ʽ
	    params.put("rate", 16000);//��Ƶ��ʽ16k��8k �����ʡ�16bit λ���������
	    params.put("channel", "1");//����������֧�ֵ�����������д�̶�ֵ 1
	    params.put("token", token);//������Ĳ�����ȡ��token
	    params.put("dev_pid", 1537);//��ͨ��ʶ�����
	    params.put("cuid", cuid);//�û�Ψһ��ʶ�����������û�������UVֵ��������д�������û��Ļ��� MAC ��ַ�� IMEI �룬����Ϊ60�ַ����ڡ�
//	    params.put("len", pcmFile.length());//�ѷ���
	    //���������ļ��ĵĶ������������� ����Ҫ����base64 ���롣��len������һ��ʹ�á�
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
	  // GBK����תΪUTF-8
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
