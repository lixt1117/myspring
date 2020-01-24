package com.lxt.java.mini.v01;

import com.lxt.java.mini.annotation.MyAutoWired;
import com.lxt.java.mini.annotation.MyController;
import com.lxt.java.mini.annotation.MyRequestMapping;
import com.lxt.java.mini.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Description: 模仿spring编写的DispatcherServlet类
 * 主要功能步骤：
 *      1.读取配置文件
 *      2.扫描路径下所有的类，进行实例化，并根据类所加的注解分别保存在不同的容器中（IOC）
 *      3.读取类的属性，对加了MyAutoWired注解的进行注入（DI）
 *      4.读取controller类下的方法，将MyRequestMapping中的路径和方法进行映射并保存在容器中
 *      (以上为启动时容器所做工作，下面为容器运行时的大致流程)
 *      5.接到Http请求后，根据调用路径名，从容器中获取方法的调用和参数集合 6.校验并组装参数，全部完成后进行方法调用
 * @Param:
 * @Return:
 * @auther: lixiaotian
 * @date: 2020/1/19 19:01
 */
public class MyDispatcherServlet extends HttpServlet {

    private Map<String, Object> mapping = new HashMap<String, Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.mapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!!");
            return;
        }
        Method method = (Method) this.mapping.get(url);
        Map<String, String[]> params = req.getParameterMap();
        // 这个版本的尴尬之处在接受请求时把方法参数写死了。。。也就是说只能响应一个方法
        Object result = method.invoke(this.mapping.get(method.getDeclaringClass().getName()),
                new Integer(params.get("a")[0]), new Integer(params.get("b")[0]));
        resp.getWriter().println(result);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream inputStream = null;
        try {
            // 读取spring配置文件的路径
            String contextConfigLocation = config.getInitParameter("contextConfigLocation");
            // 读取配置文件
            Properties properties = new Properties();
            inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                System.out.println("读取配置文件失败！");
                e.printStackTrace();
            }
            String scanPackage = properties.getProperty("scanPackage");
            // 扫描路径下的class文件并将class路径名保存
            doScanner(scanPackage);
            // 1.遍历扫描到的class名字，实例化class，并保存类的实例
            // 2.读取controller层的方法，并将URL和method实例的映射进行保存
            // 这里原本是直接遍历mapping.keySet()的，结果运行出现了ConcurrentModificationException
            for (String clazzName : new ArrayList<String>(mapping.keySet())) {
                Class myClass = null;
                try {
                    myClass = Class.forName(clazzName);
                } catch (ClassNotFoundException e) {
                    System.out.println(clazzName + "创建class失败！");
                    e.printStackTrace();
                }
                try {
                    // 如果class是controller，则实例化并保存method映射,如果是service则进行实例化
                    if (myClass.isAnnotationPresent(MyController.class)) {
                        MyController myController = (MyController) myClass.getAnnotation(MyController.class);
                        // 获取类的public方法
                        Method[] methods = myClass.getMethods();
                        for (int i = 0; i < methods.length; i++) {
                            if (methods[i].isAnnotationPresent(MyRequestMapping.class)) {
                                // 方法的路径
                                String methodPath = myController.value()
                                        + methods[i].getAnnotation(MyRequestMapping.class).value();
                                methods[i].setAccessible(true);
                                // 保存url和method的映射
                                this.mapping.put(methodPath, methods[i]);
                            }
                        }
                        this.mapping.put(clazzName, myClass.newInstance());
                    } else if (myClass.isAnnotationPresent(MyService.class)) {
                        this.mapping.put(clazzName, myClass.newInstance());
                    }
                } catch (InstantiationException e) {
                    System.out.println(clazzName + "实例化异常！");
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    System.out.println(clazzName + "实例化异常！");
                    e.printStackTrace();
                }
            }
            // 类实例化完成后，开始DI（自动注入）
            for (Object object : mapping.values()) {
                if (object == null || object instanceof Method) {
                    continue;
                }
                Class clazz = object.getClass();
                // 获取对象的所有属性
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!field.isAnnotationPresent(MyAutoWired.class)) {
                        continue;
                    }
                    MyAutoWired autowired = field.getAnnotation(MyAutoWired.class);
                    String beanName = autowired.value();
                    if ("".equals(beanName)) {
                        beanName = field.getType().getName();
                    }
                    field.setAccessible(true);
                    try {
                        field.set(mapping.get(clazz.getName()), mapping.get(beanName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.print("My MVC Framework is init");
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
                mapping.put(clazzName, null);
            }
        }
    }
}
