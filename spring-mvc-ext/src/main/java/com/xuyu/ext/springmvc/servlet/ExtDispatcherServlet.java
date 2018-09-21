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
public class ExtDispatcherServlet extends HttpServlet{

	//ɨ����Χ
	private  String packageName="com.xuyu.ext.springmvc.controller";
	//springmvc�������� keyΪ����id��valueΪ�����
	private ConcurrentHashMap<String, Object>springmvcBeans=new ConcurrentHashMap<String, Object>();
	//springmvc�������� keyΪ�����ַ��valueΪ�����
	private ConcurrentHashMap<String, Object>urlBeans=new ConcurrentHashMap<String, Object>();
	//springmvc �������� keyΪ�����ַ��valueΪ��������
	private ConcurrentHashMap<String, String>urlMethods=new ConcurrentHashMap<String, String>();

	@Override
	public void init() throws ServletException {
		//1.��ȡ��ǰ���µ�������
		List<Class<?>> classes = ClassUtil.getClasses(packageName);
		//2.�ж������Ƿ���ע�⣬ʹ��Java�������ѭ�����������Ƿ���ע�⣬���з�װurl�ͷ�����Ӧ
		try {
			findClassMvcAnnotation(classes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//3.��urlӳ��ͷ������й���
		handlerMapping();
	}
	public void findClassMvcAnnotation(List<Class<?>> classes) throws Exception {
		for (Class<?> classInfo : classes) {
			//�ж������Ƿ����ע��
			ExtController extControllerAnnotation = classInfo.getDeclaredAnnotation(ExtController.class);
			if(extControllerAnnotation!=null) {
				//������תСд����Ĭ�ϵ����Ӧ��beanid
				String beanId = ClassUtil.toLowerCaseFirstOne(classInfo.getSimpleName());
				//���÷�����Ƴ�ʼ���õ�ʵ��������
				Object beanObject = ClassUtil.newInstance(classInfo);
				//������ϼ���ע�⣬�Ͱ���ŵ�map������
				springmvcBeans.put(beanId, beanObject);
			}
		}
	}
	//��url�ͷ������й���
	public void handlerMapping() {
		//1.��ȡspringmvcBeans bean����������
		//2.����springmvcBeans bean���� �ж������Ƿ���urlӳ��ע��
		for (Map.Entry<String, Object> springmvcBeanObject : springmvcBeans.entrySet()) {
			//3.�������з������Ƿ���urlӳ��ע�� ��ȡbean����
			Object beanObject = springmvcBeanObject.getValue();
			//4.�ж������Ƿ������URLӳ��ע��
			Class<? extends Object> classInfo = beanObject.getClass();
			ExtRequestMapping extRequestMapping = classInfo.getDeclaredAnnotation(ExtRequestMapping.class);
			String beanClassUrl ="";
			if(extRequestMapping!=null) {
				//��ȡ���ϵ�urlӳ���ַ
				beanClassUrl = extRequestMapping.value();
			}
			//4.�жϷ������Ƿ��urlӳ���ַ
			Method[] declaredMethods = classInfo.getDeclaredMethods();
			for (Method method : declaredMethods) {
				//�жϷ������Ƿ����urlӳ��ע��
				ExtRequestMapping methodExTRequestMapping = method.getDeclaredAnnotation(ExtRequestMapping.class);
				if(methodExTRequestMapping!=null) {
					//��ȡ�����ϵ�urlӳ���ַ
					String beanMethodUrl = methodExTRequestMapping.value();
					//��ȡ��������
					String methodName = method.getName();
					//urlƴ��
					String realUrl=beanClassUrl+beanMethodUrl;
					//����ַ����������map������
					urlBeans.put(realUrl, beanObject);
					urlMethods.put(realUrl, methodName);
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
		//2.��map�����л�ȡ���ƶ���
		Object object = urlBeans.get(requestURI);
		if(object==null) {
			resp.getWriter().println("not found 404 url");
			return;
		}
		//3.ʹ��url��ַ��ȡ����
		String methodName = urlMethods.get(requestURI);
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
			Class<? extends Object> classInfo = object.getClass();
			Method method = classInfo.getMethod(methodName);
			Object invokeResult = method.invoke(object);
			return invokeResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
