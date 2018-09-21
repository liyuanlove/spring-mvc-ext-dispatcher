package com.xuyu.ext.springmvc.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.xuyu.ext.springmvc.exannotation.ExtController;
import com.xuyu.ext.springmvc.exannotation.ExtRequestMapping;
import com.xuyu.ext.springmvc.utils.ClassUtil;

/** �Զ���ǰ�˿�����

 * ��дspringmvcע��汾ԭ��
 * 1.����һ��ǰ�˿���������ExtDispatcherServlet ������������
 * 2.��ʼ������ ��дservlet ��init ����
 * 		2.1��ʼɨ������ɨ����Χ��������ע�뵽springmvc�������棬���map������keyΪĬ������Сд��valueΪ����
 * 		2.2��URLӳ��ͷ������й���
 * 			2.2.1�ж������Ƿ���ע�⣬ʹ��Java�������ѭ�����������Ƿ���ע�⣬���з�װurl�ͷ�����Ӧ
 * 3.�������� ��дGet����Post����
 * 		3.1��ȡ����url��ȥurlBeans�����л�ȡʵ�����󣬻�ȡ�ɹ�ʵ������󣬵���urlMethods���ϻ�ȡ�������ƣ�ʹ�÷������ִ��
 * 
 * @author Administrator
 *
 */
//��һ��������һ��ǰ�˿����� ExtDispatcherServlet �̳�HttpServlet
public class ExtDispatcherServlet extends HttpServlet{

	//����ɨ����Χ
	private  String packageName="com.xuyu.ext.springmvc.controller";
	/**
	 * new 3��map��������
	 */
	//springmvc�������� keyΪ����id��valueΪ�����
	private ConcurrentHashMap<String, Object>map1=new ConcurrentHashMap<String, Object>();
	//springmvc�������� keyΪ�����ַ��valueΪ�����
	private ConcurrentHashMap<String, Object>map2=new ConcurrentHashMap<String, Object>();
	//springmvc �������� keyΪ�����ַ��valueΪ��������
	private ConcurrentHashMap<String, String>map3=new ConcurrentHashMap<String, String>();

	/**
	 * ����������дHttpServlet��init()����
	 */
	@Override
	public void init() throws ServletException {
		//1.ɨ��ȥ��õ�ǰ���µ�������
		List<Class<?>> classes = ClassUtil.getClasses(packageName);
		try {
			//2.�ж������Ƿ����ExtControllerע��
			findClassMvcAnnotation(classes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//3.��urlӳ��ͷ������й���
		handlerMapping();
	}
	public void findClassMvcAnnotation(List<Class<?>> classes) throws Exception {
		for (Class<?> classInfo : classes) {
			ExtController extControllerAnnotation = classInfo.getDeclaredAnnotation(ExtController.class);
			//3.������ϴ������Ӧ��ע��
			if(extControllerAnnotation!=null) {
				//�ٽ���������ĸתСд��ΪbeanId
				String beanId = ClassUtil.toLowerCaseFirstOne(classInfo.getSimpleName());
				//��ʹ��java�������ʵ��������õ�beanObject
				Object beanObject = ClassUtil.newInstance(classInfo);
				//�۰Ѷ����beanId��Ϊkey��beanObject��Ϊvalue��map1����
				map1.put(beanId, beanObject);
			}
		}
	}
	//����������urlӳ��ͷ������й���
	public void handlerMapping() {
		for (Map.Entry<String, Object> map1BeanObject : map1.entrySet()) {
			//1.��map1������ѭ��������ȡbeanObjectʵ������
			Object beanObject = map1BeanObject.getValue();
			//2.ͨ��beanObjectʵ������õ���������
			Class<? extends Object> classInfo = beanObject.getClass();
			//3.ͨ�������������ȥ���ExtRequestMappingע��
			ExtRequestMapping extRequestMapping = classInfo.getDeclaredAnnotation(ExtRequestMapping.class);
			String beanClassUrl ="";
			//4.������ϴ������Ӧ��ע��
			if(extRequestMapping!=null) {
				//��ȡ������ϵ�ע���valueֵ���Ƕ�Ӧurlӳ���ַ
				beanClassUrl = extRequestMapping.value();
			}
			//5.�����������Ϣȥ��ȡ������µ����з���
			Method[] declaredMethods = classInfo.getDeclaredMethods();
			//6.ѭ�������õ��ķ���
			for (Method method : declaredMethods) {
				//6.1���ݷ�������ȥ������������ϵ�ExtRequestMappingע��
				ExtRequestMapping methodExTRequestMapping = method.getDeclaredAnnotation(ExtRequestMapping.class);
				//6.2.�����������ExtRequestMappingע��
				if(methodExTRequestMapping!=null) {
					//1.������������ע���valueֵ���Ƕ�Ӧ��urlӳ���ַ
					String beanMethodUrl = methodExTRequestMapping.value();
					//2.��ȡ��������
					String methodName = method.getName();
					//3.urlƴ��
					String realUrl=beanClassUrl+beanMethodUrl;
					//4.����ַ�������ʵ������map2������
					map2.put(realUrl, beanObject);
					//5.����ַ�ͷ������ƴ�map3������
					map3.put(realUrl, methodName);
				}
			}
		}
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		doPost(req,resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		//####################��������###########################
		//1.��ȡurl��ַ
		String requestURI = req.getRequestURI();
		if(StringUtils.isEmpty(requestURI)) {
			return;
		}
		//2.��map2������ʹ��url��ַ��ȡ����ʵ��
		Object object = map2.get(requestURI);
		if(object==null) {
			resp.getWriter().println("not found 404 url");
			return;
		}
		//3.��map3������ʹ��url��ַ��ȡ��������
		String methodName = map3.get(requestURI);
		if(StringUtils.isEmpty(methodName)) {
			resp.getWriter().println("not found 404 methodName");
		}
		//4.ʹ��java������Ƶ��÷���
		String methodInvokeResultPage = (String) methodInvoke(object,methodName);
		//5.ʹ��Java������ƻ�ȡ�������ؽ��
		resp.getWriter().println(methodInvokeResultPage);
		//6.������ͼת������Ⱦ��ҳ��չʾ
		extResourceViewResolver(methodInvokeResultPage,req,resp);
		
	}
	private void extResourceViewResolver(String pageName,HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException  {
		String prefix="/";
		String suffix=".jsp";
		req.getRequestDispatcher(prefix+pageName+suffix).forward(req, res);
	}
	private Object methodInvoke(Object object,String methodName) {
		try {
			//1.��ȡ��������
			Class<? extends Object> classInfo = object.getClass();
			//2.ͨ���������ͻ�÷�������
			Method method = classInfo.getMethod(methodName);
			//ͨ���������Ʒ����ȡ����
			Object invokeResult = method.invoke(object);
			//���ط������
			return invokeResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
