package com.lxt.java.mini.v03;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lxt.java.mini.annotation.*;
import com.lxt.java.mini.constants.HttpResponseEnum;
import com.lxt.java.mini.exception.HttpException;
import com.lxt.java.mini.util.StringUtils;
import com.lxt.java.mini.v02.MyMethod;
import com.lxt.java.mini.v03.convert.MyConvertEnum;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/24 22:10
 * @Description:模仿spring编写的DispatcherServlet类
 * 相较于v2版本，做了以下改进：
 *      1.解决请求参数动态类型强转问题
 *      2.像SpringMVC 一样支持正则
 *      3.hadnlerMapping改用list结构，原因是既然像spring的url支持正则，而map里的key（url
 *      ）不能重复，既然怎么都要遍历，还不如直接上list
 */
public class MyDispatcherServlet extends HttpServlet {

    // spring配置文件
    private Properties properties = new Properties();

    // 扫描的包路径
    private String scannerPath = "";

    // 保存class名字的容器
    private List<String> classList = new ArrayList<>();

    // 保存Controller的容器
    private Map<String, Object> controllerMapping = new HashMap<>();

    // 保存service的容器
    private Map<String, Object> serviceMapping = new HashMap<>();

    // 保存方法路径和method实例的容器
    // private Map<String, Object> handlerMapping = new HashMap<>();
    private List<MyHandler> handlerMapping = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (HttpException e) {
            if (e.getHttpCode() == HttpResponseEnum.CODE_404) {
                resp.getWriter().write("404 Not Found!!");
                e.printStackTrace();
                return;
            }
            resp.getWriter().write(e.getHttpCode() + " Exception \n" + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws HttpException {
        try {
            // 获取handler
            MyHandler myHandler = getHandler(req);
            if (null != myHandler) {
                // 获取参数
                Map<String, String[]> paramMap = req.getParameterMap();
                // Method实例
                Method method = myHandler.getMethod();
                // 参数名称数组
                String[] paramNames = myHandler.getParamNames();
                // 方法所在的controller实例
                Object controller = myHandler.getController();
                // 参数类型数组
                Class[] paramTypes = method.getParameterTypes();
                // 反射调用方法传入的参数数组
                Object[] params = new Object[paramTypes.length];
                for (int i = 0; i < paramNames.length; i++) {
                    // 没有名的参数目前只有HttpServletRequest和HttpServletResponse
                    if (null == paramNames[i]) {
                        // class直接==比较即可（感谢双亲委派）
                        if (paramTypes[i] == HttpServletRequest.class) {
                            params[i] = req;
                        } else if (paramTypes[i] == HttpServletResponse.class) {
                            params[i] = resp;
                        }
                        continue;
                    }
                    if (paramMap.containsKey(paramNames[i])) {
                        // 参数值数组
                        String[] paramvalues = paramMap.get(paramNames[i]);
                        for (String paramvalue : paramvalues) {
                            // 调用枚举里定义的方法，实现参数类型动态转换
                            MyConvertEnum myConvertEnum = MyConvertEnum
                                    .valueOf(paramTypes[i].getSimpleName().toUpperCase());
                            params[i] = myConvertEnum.convert(paramvalue);
                            // params[i] = paramvalue;
                        }
                    } else {
                        // 请求参数不全，返回400
                        throw new HttpException(HttpResponseEnum.CODE_400);
                    }
                }
                Object result = method.invoke(controller, params);
                if (null == result) {
                    return;
                } else {
                    resp.getWriter().write(result.toString());
                }
            } else {
                // 容器中无调用方法，返回404
                throw new HttpException(HttpResponseEnum.CODE_404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpException httpException;
            if (e instanceof HttpException) {
                httpException = new HttpException(((HttpException) e).getHttpCode());
            } else {
                httpException = new HttpException(HttpResponseEnum.CODE_500);
            }
            httpException.setStackTrace(e.getStackTrace());
            throw httpException;
        }
    }

    // 根据url遍历获取handler
    private MyHandler getHandler(HttpServletRequest req) throws Exception {
        // 请求路径，如：/myspring_war/member/getMemberInfoByName?name=lixiaotian
        String requestURI = req.getRequestURI();
        // 上下文路径，debug看好像是项目根路径，如：/myspring_war
        String contextPath = req.getContextPath();
        // 获取方法url
        requestURI = requestURI.replace(contextPath, "").replaceAll("/+", "/");
        for (MyHandler myHandler : handlerMapping) {
            if (!myHandler.getUrlPattern().matcher(requestURI).matches()) {
                continue;
            }
            return myHandler;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            // 1.读取配置文件
            readApplicationContext(config);
            // 2.扫描路径下所有的类
            doscanner(scannerPath);
            // 3.根据类的功能（注解）分别保存在不同的IOC容器中
            initClasses();
            // 4.读取类的属性，对加了MyAutoWired注解的进行注入（DI）
            doDenpendInjection();
            // 5.读取controller类下的方法，将MyRequestMapping中的路径和方法进行映射并保存在容器中
            // (初始化HandlerMapping）
            initHandlerMapping();
            // 6.初始化完成
            System.out.println("My MVC Framework is init");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readApplicationContext(ServletConfig config) throws Exception {
        InputStream inputStream = null;
        try {
            String contextConfigLocation = config.getInitParameter("contextConfigLocation");
            inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            properties.load(inputStream);
            scannerPath = properties.getProperty("scanPackage");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private void doscanner(String scannerpath) throws Exception {
        if (!StringUtils.isEmpty(scannerpath)) {
            URL url = this.getClass().getClassLoader().getResource("/" + scannerpath.replaceAll("\\.", "/"));
            File file = new File(url.getFile());
            File[] files = file.listFiles();
            for (File eachFile : files) {
                // 如果是文件夹则递归调用自己
                if (eachFile.isDirectory()) {
                    this.doscanner(scannerpath + "." + eachFile.getName());
                } else {
                    if (eachFile.getName().endsWith(".class")) {
                        // 对于class文件，去掉文件后缀保存进IOC容器中
                        String clazzName = eachFile.getName().replace(".class", "");
                        classList.add(scannerpath + "." + clazzName);
                    }
                }
            }
        }
    }

    private void initClasses() throws Exception {
        // 遍历class集合，进行初始化
        for (String clazzName : classList) {
            Class clazz = Class.forName(clazzName);
            if (null != clazz) {
                if (clazz.isAnnotationPresent(MyController.class)) {
                    Object object = clazz.newInstance();
                    controllerMapping.put(StringUtils.toLowercaseFirstLetter(clazz.getSimpleName()), object);
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    Object object = clazz.newInstance();
                    serviceMapping.put(StringUtils.toLowercaseFirstLetter(clazz.getSimpleName()), object);
                }
            }
        }
    }

    // 依赖注入
    // 这里思考一个问题，我的spring如何解决循环依赖的问题（貌似我这种写法太低级还没涉及到循环依赖。。。）
    private void doDenpendInjection() throws Exception {
        for (Object object : controllerMapping.values()) {
            Class clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(MyAutoWired.class)) {
                    MyAutoWired myAutoWired = field.getAnnotation(MyAutoWired.class);
                    field.setAccessible(true);
                    field.set(object, serviceMapping
                            .get(StringUtils.isEmpty(myAutoWired.value()) ? field.getName() : myAutoWired.value()));
                }
            }
        }
        // 其实这里应该还有个遍历，对service进行注入，不过我demo里的service里没有mapper和其他service，就不写了
    }

    private void initHandlerMapping() throws Exception {
        for (Object object : controllerMapping.values()) {
            Class clazz = object.getClass();
            MyController myController = (MyController) clazz.getAnnotation(MyController.class);
            // 注释掉这样是因为StringBuilder.append会更新原来的StringBuilder，即便你拿另一个StringBuilder接受也是一样
            // StringBuilder methodUrl = new
            // StringBuilder(myController.value());
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(MyRequestMapping.class)) {
                    MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
                    StringBuilder newMethodUrl = new StringBuilder(myController.value())
                            .append(myRequestMapping.value());
                    // 获取方法参数的注解
                    Annotation[][] annotations = method.getParameterAnnotations();
                    // 方法参数名称
                    String[] paramNames = new String[method.getParameterCount()];
                    // 遍历注解数组，找出参数的MyRequestMapping注解的name
                    for (int i = 0; i < annotations.length; i++) {
                        // 每个参数所持有的注解
                        Annotation[] eachAnnotations = annotations[i];
                        // 注解参数名
                        String paramName = null;
                        // 这里判空而不直接遍历是因为得到的二维数组可能不是完整的，
                        // 比如第一个参数一个注解都没有的话，eachAnnotations[0][0]会直接报数组下标越界
                        if (eachAnnotations.length != 0) {
                            for (int j = 0; j < eachAnnotations.length; j++) {
                                if (eachAnnotations[j] instanceof MyRequestParam) {
                                    MyRequestParam myRequestParam = (MyRequestParam) eachAnnotations[j];
                                    // 注解参数名
                                    paramName = myRequestParam.value();
                                }
                            }
                        }
                        paramNames[i] = paramName;
                    }
                    // url转正则
                    Pattern pattern = Pattern.compile(newMethodUrl.toString());
                    MyHandler myHandler = new MyHandler(pattern, method, paramNames, object);
                    // MyMethod myMethod = new MyMethod(method, paramNames,
                    // StringUtils.toLowercaseFirstLetter(clazz.getSimpleName()));
                    handlerMapping.add(myHandler);
                }
            }
        }
    }
}
