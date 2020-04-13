package com.lagou.edu.factory;

import com.lagou.edu.annotation.AutowiredAnnotation;
import com.lagou.edu.annotation.ServiceAnnotation;
import com.lagou.edu.annotation.TransactionalAnnotation;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc 注解加载类
 * @author zgq
 * @date 2020/4/13 11:53
 */
public class AnnotationBeanFactory {
    private static Map<String, Object> map = new HashMap<>();  // 存储对象

    private static List<Class> clazzs = new ArrayList<>();

    static {
        //默认扫描整个包
        String packagePath = "com.lagou.edu";
        try {
            //获取包下的所有class对象
            getClazz(packagePath, clazzs);
            //从类中找到加了注解的类并并创建代理对象，放到map中
            getAnnotationClazz();
            //依赖注入
            getDeclareFieldsAnnotation();
            //声明式事物创建代理对象
            transactionalProxyObj();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @desc 获取包下的所有class对象
     * @author zgq
     * @date 2020/4/13 11:54
     */
    public static void getClazz(String packagePath, List<Class> clazzs) throws ClassNotFoundException {
        packagePath = packagePath.replaceAll("\\.", "/");
        File file = new File(AnnotationBeanFactory.class.getClassLoader().getResource(packagePath).getFile());
        File[] files = file.listFiles();
        for (File file1 : files) {
            //如果是目录，这进一个寻找
            if (file1.isDirectory()) {
                //截取路径最后的文件夹名
                String currentPathName = file1.getAbsolutePath().substring(file1.getAbsolutePath().lastIndexOf(File.separator) + 1);
                //进一步寻找
                getClazz(packagePath + "." + currentPathName, clazzs);
            } else {
                //如果是class文件
                if (file1.getName().endsWith(".class")) {
                    //反射出实例
                    packagePath = packagePath.replaceAll("/", "\\.");
                    Class clazz = Thread.currentThread().getContextClassLoader().loadClass(packagePath + "." + file1.getName().replace(".class", ""));
                    clazzs.add(clazz);
                }
            }
        }
    }

    /**
     * @desc 从类中找到加了注解的类并并创建代理对象，放到map中
     * @author zgq
     * @date 2020/4/13 11:55
     */
    public static void getAnnotationClazz() throws IllegalAccessException, InstantiationException {
        for (Class clazz : clazzs) {
            //判断该类是否存在指定的注解
            if (clazz.isAnnotationPresent(ServiceAnnotation.class)) {
                //获取注解的属性
                ServiceAnnotation annotation = (ServiceAnnotation) clazz.getAnnotation(ServiceAnnotation.class);
                String value = annotation.value();
                if (value == null) {
                    //如果不存在则value 为 类名的首字母小写
                    value = new StringBuilder(Character.toLowerCase(clazz.getName().charAt(0))).append(clazz.getName().substring(1)).toString();
                }
                Object o = clazz.newInstance();  // 实例化之后的对象
                // 存储到map中待用
                map.put(value, o);
            }
        }
    }


    /**
     * @desc 依赖注入
     * @author zgq
     * @date 2020/4/13 11:55
     */
    public static void getDeclareFieldsAnnotation() throws IllegalAccessException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            //获取类中所有的属性
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(AutowiredAnnotation.class)) {
                    //获取注解的属性
                    AutowiredAnnotation annotation = field.getAnnotation(AutowiredAnnotation.class);
                    String value = annotation.value();
                    if (value == null) {
                        //如果不存在则value 为 类名的首字母小写
                        value = new StringBuilder(Character.toLowerCase(field.getName().charAt(0))).append(field.getName().substring(1)).toString();
                    }
                    field.setAccessible(true); // 设置些属性是可以访问的
                    field.set(entry.getValue(), map.get(value));
                }
            }
        }
    }


    /**
     * @desc 代理对象的产生
     * @author zgq
     * @date 2020/4/13 11:55
     */
    public static void transactionalProxyObj() {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Method[] methods = entry.getValue().getClass().getMethods();
            //判断类中方法是否添加了@TransactionalAnnotation对象则生成该类的代理对象,这里简单实现，只要出现该注解就将所有的方法加事物
            for (Method method : methods) {
                if (method.isAnnotationPresent(TransactionalAnnotation.class)) {
                    Class[] interfaces = entry.getClass().getInterfaces();
                    if (interfaces.length == 0) {
                        //使用CGLib创建代理对象
                        ProxyFactory proxyFactory = (ProxyFactory) map.get("proxyFactory");
                        Object cglibProxy = proxyFactory.getCglibProxy(entry.getValue());
                        map.put(entry.getKey(), cglibProxy);
                    } else {
                        //使用JDK创建代理对象
                        ProxyFactory proxyFactory = (ProxyFactory) map.get("proxyFactory");
                        Object jdkProxy = proxyFactory.getJdkProxy(entry.getValue());
                        map.put(entry.getKey(), jdkProxy);
                    }
                }
            }
        }
    }

    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static Object getBean(String id) {
        return map.get(id);
    }
}
