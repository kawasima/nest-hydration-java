package net.unit8.hydration.mapping;

public class ColumnProperty {
    private String column;
    private String type;
    private boolean id;
    private String defaultValue;

    public ColumnProperty(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
