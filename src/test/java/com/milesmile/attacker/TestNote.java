package com.milesmile.attacker;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by chris.D on 2020/1/22.
 * Email chris.dong101@gmail.com
 * 参考文章 https://blog.csdn.net/suan__nai/article/details/96862248
 */
public class TestNote {
    @Test
    public void testClass() throws ClassNotFoundException {
        //获取类的Class对象class（描述类的对象）（类字节码）
        Class cls1 = Class.forName("com.milesmile.attacker.TestNote$User");
        Class cls2 = com.milesmile.attacker.TestNote.User.class;
        User user = new User();
        Class cls3 = user.getClass();
        //cls1 cls2 cls3的值均为class class java.lang.Runtime
        System.out.print(cls1 + "  " + cls2 + "  " + cls3);
    }

    @Test
    public void testConstructor() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        //将类字节码实例化,便能调用对象中的方法
        Class cls1 = Class.forName("com.milesmile.attacker.TestNote$User");
        //该用例为内部类构造方法
        Constructor[] constructor = cls1.getConstructors();
        User user = (User) constructor[1].newInstance(new TestNote(), "test", 18);
        System.out.println(user.getName());
    }

    @Test
    public void testMethod() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Class cls1 = Class.forName("com.milesmile.attacker.TestNote$User");
        //该用例为内部类构造方法
        Constructor[] constructor = cls1.getConstructors();
        User user = (User) constructor[1].newInstance(new TestNote(), "testMethod", 18);
        //获取对象中方法并用invoke执行
        Method method1 = cls1.getMethod("getName");
        method1.invoke(user);
        Method method2 = cls1.getMethod("setAge", int.class);
        method2.invoke(user, 21);

        System.out.println(user.toString());
    }

    @Test
    public void testFiled() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        Class cls1 = Class.forName("com.milesmile.attacker.TestNote$User");
        //该用例为内部类构造方法
        Constructor[] constructor = cls1.getConstructors();
        User user = (User) constructor[1].newInstance(new TestNote(), "testMethod", 18);
        //获取成员变量
        Field field = cls1.getDeclaredField("name");
        field.setAccessible(true);
        field.set(user, "test");
        //获取全部成员变量
        Field[] fields = cls1.getDeclaredFields();
        for (Field i : fields) {
            i.setAccessible(true);
            System.out.println(i.getName() + ":" + i.get(user));
        }
    }

    public class User implements Serializable {
        private String name;
        private int age;

        public User() {
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
            System.out.println("create user done");
        }

        public String getName() {
            return this.name;
        }

        public int getAge() {
            return this.age;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String toString() {
            return this.name + " : " + this.age;
        }
    }
}
