package net.unit8.hydration;

import java.io.Serializable;

public class Value implements Serializable {
    private String prop;
    private String column;
    private String type;
    private Object defaultValue;

    public Value(String prop, String column, String type, Object defaultValue) {
        this.prop = prop;
        this.column = column;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getProp() {
        return prop;
    }

    public String getColumn() {
        return column;
    }

    public String getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
