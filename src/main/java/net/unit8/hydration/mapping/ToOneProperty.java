package net.unit8.hydration.mapping;

import java.io.Serializable;

public class ToOneProperty implements Serializable {
    private String prop;
    private String column;

    public ToOneProperty(String prop, String column) {
        this.prop = prop;
        this.column = column;
    }

    public String getProp() {
        return prop;
    }

    public String getColumn() {
        return column;
    }
}
