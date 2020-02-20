package net.unit8.hydration.mapping;

import java.io.Serializable;

import static java.util.Objects.nonNull;

public class ValueProperty implements Serializable {
    private String prop;
    private String column;
    private String type;
    private String defaultValue;

    public ValueProperty(ColumnProperty colProp, String prop) {
        this.prop = prop;
        this.column = colProp.getColumn();
        this.type = colProp.getType();
        this.defaultValue = colProp.getDefaultValue();
    }

    public boolean hasDefaultValue() {
        return nonNull(defaultValue);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getColumn() {
        return column;
    }

    public String getProp() {
        return prop;
    }

    public String getType() {
        return type;
    }
}
