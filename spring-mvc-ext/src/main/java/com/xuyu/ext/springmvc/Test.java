package com.xuyu.ext.springmvc;
/**
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

import java.util.concurrent.ConcurrentHashMap;

public class Test {

	//springmvc�������� keyΪ����id��valueΪ�����
	private ConcurrentHashMap<String, Object>springmvcBeans=new ConcurrentHashMap<String, Object>();
	//springmvc�������� keyΪ�����ַ��valueΪ����
	private ConcurrentHashMap<String, Object>urlBeans=new ConcurrentHashMap<String, Object>();
	//springmvc �������� keyΪ�����ַ��valueΪ��������
	private ConcurrentHashMap<String, Object>urlMethods=new ConcurrentHashMap<String, Object>();

}
