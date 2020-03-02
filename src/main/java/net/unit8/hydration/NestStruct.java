package net.unit8.hydration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NestStruct {
    private Object struct;

    public NestStruct() {

    }

    public boolean isEmpty() {
        return struct == null;
    }

    public void initializeAsList() {
        struct = new ArrayList<>();
    }

    public void add(Map<String, Object> obj) {
        ((List<Object>) struct).add(obj);
    }

    public void set(Map<String, Object> obj) {
        struct = obj;
    }

    public Object getObject() {
        return struct;
    }
}
