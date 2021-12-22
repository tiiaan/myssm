package com.tiiaan.ssm.myssm.ioc;

import com.tiiaan.ssm.myssm.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description TODO
 */

public class ClassPathXmlApplicationContext implements BeanFactory {

    private Map<String, Object> beanMap = new HashMap<>();


    public ClassPathXmlApplicationContext() {
        this("applicationContext.xml");
    }


    public ClassPathXmlApplicationContext(String dir) {

        if (StringUtils.isNullOrEmpty(dir)) {
            throw new RuntimeException("can not find the applicationContext.xml.");
        }

        try {
            //解析 applicationContext.xml 文件
            //1.InputStream
            InputStream is = getClass().getClassLoader().getResourceAsStream(dir);

            //2.newInstance() 获取 DocumentBuilderFactory 对象
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            //3.获取 DocumentBuilder 对象
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            //4.获取 Document 对象
            Document document = documentBuilder.parse(is);

            //5.获取所有的 bean 节点, 存储到 beanMap<String, Object>
            NodeList beanNodes = document.getElementsByTagName("bean");
            for (int i = 0; i < beanNodes.getLength(); i++) {
                Node beanNode = beanNodes.item(i);
                if (beanNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element beanElement = (Element) beanNode;
                    String beanId = beanElement.getAttribute("id");
                    String className = beanElement.getAttribute("class");
                    Object beanObj = Class.forName(className).newInstance();
                    beanMap.put(beanId, beanObj);
                }
            }

            //注入依赖关系
            for (int i = 0; i < beanNodes.getLength(); i++) {
                Node beanNode = beanNodes.item(i);
                if (beanNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element beanElement = (Element) beanNode;

                    //遍历bean节点, 获取它们的property子节点
                    NodeList childNodes = beanElement.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            if ("property".equals(childNode.getNodeName())) {
                                Element childElement = (Element) childNode;

                                //获取属性名, 和引用的其他bean对象的ref_id
                                String propertyName = childElement.getAttribute("name");
                                String propertyRef = childElement.getAttribute("ref");

                                //在beanMap中找到id=ref的类的对象refObj
                                Object refObj = beanMap.get(propertyRef);

                                //在beanMap中通过id找到当前类的对象beanObj, 再通过propertyName找到当前类中的属性
                                String beanId = beanElement.getAttribute("id");
                                Object beanObj = beanMap.get(beanId);
                                Field propertyField = beanObj.getClass().getDeclaredField(propertyName);

                                //把引用的对象赋给属性
                                propertyField.setAccessible(true);
                                propertyField.set(beanObj, refObj);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Object getBean(String id) {
        return beanMap.get(id);
    }
}
