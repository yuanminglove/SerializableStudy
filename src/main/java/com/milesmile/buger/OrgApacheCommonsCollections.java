package com.milesmile.buger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by chris.D on 2020/1/21.
 * Email chris.dong101@gmail.com
 */
public class OrgApacheCommonsCollections implements Serializable {
    // name可有可无，又不是真重写
    public String name;
    public Map map;

    private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException , IOException {
        in.defaultReadObject();
        if(map != null){
            Map.Entry e = (Map.Entry)map.entrySet().iterator().next();
            e.setValue("400m"); //在map赋值中触发漏洞
        }
    }

}
