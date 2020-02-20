package net.unit8.hydration.mapping;

import java.util.*;
import java.util.stream.Collectors;

public class PropertyMapping {
    private boolean isOneToMany;
    private LinkedHashSet<String> keys;
    private Map<String, ColumnProperty> columnProperties;
    private Map<String, PropertyMapping> toManyProperties;
    private Map<String, PropertyMapping> toOneProperties;

    private PropertyMapping(boolean isOneToMany) {
        this.isOneToMany = isOneToMany;
        keys = new LinkedHashSet<>();
        columnProperties = new HashMap<>();
        toManyProperties = new HashMap<>();
        toOneProperties = new HashMap<>();
    }

    public static PropertyMapping createBase() {
        PropertyMapping propertyMapping = new PropertyMapping(false);
        propertyMapping.keys.add("base");
        return propertyMapping;
    }
    public static PropertyMapping createToOne() {
        return new PropertyMapping(false);
    }

    public static PropertyMapping createToMany() {
        return new PropertyMapping(true);
    }

    public boolean isOneToMany() {
        return isOneToMany;
    }

    public void put(String prop, ColumnProperty property) {
        keys.add(prop);
        columnProperties.put(prop, property);
    }

    public void putToMany(String prop, PropertyMapping mapping) {
        keys.add(prop);
        toManyProperties.put(prop, mapping);
    }

    public boolean hasToMany(String prop) {
        return toManyProperties.containsKey(prop);
    }

    public PropertyMapping getToMany(String prop) {
        return toManyProperties.get(prop);
    }

    public void putToOne(String prop, PropertyMapping mapping) {
        keys.add(prop);
        toOneProperties.put(prop, mapping);
    }

    public boolean hasToOne(String prop) {
        return toOneProperties.containsKey(prop);
    }

    public PropertyMapping getToOne(String prop) {
        return toOneProperties.get(prop);
    }

    public List<String> keys() {
        return keys.stream().collect(Collectors.toList());
    }

    public boolean hasChild(String prop) {
        return toManyProperties.containsKey(prop) || toOneProperties.containsKey(prop);
    }

    public PropertyMapping getChild(String prop) {
        return Optional.ofNullable(toManyProperties.get(prop))
                .orElse(toOneProperties.get(prop));
    }

    public boolean hasColumnProperty(String prop) {
        return columnProperties.containsKey(prop);
    }

    public ColumnProperty getColumnProperty(String prop) {
        return columnProperties.get(prop);
    }

    /**
     *
     * @param columnList The list of column names
     */
    public static PropertyMapping structPropToColumnMapFromColumnHints(List<String> columnList) {
        Map<String, Object> typeHandlers = new HashMap<>();
        PropertyMapping propertyMapping = PropertyMapping.createBase();

        for (String column : columnList) {
            String[] columnType = column.split("___");

            boolean isId = false;
            String type = null;
            for (String token : columnType) {
                if (token.equals("ID")) {
                    isId = true;
                } else if (Objects.nonNull(typeHandlers.get(token))) {
                    type = token;
                }
            }

            PropertyMapping pointer = propertyMapping;
            String prop = "base";

            final String[] navList = columnType[0].split("_");
            for (int i = 0; i< navList.length; i++) {
                String nav = navList[i];
                if (nav.isEmpty()) {
                    if (!pointer.hasChild(prop)) {
                        pointer.putToMany(prop, PropertyMapping.createToMany());
                    }
                } else {
                    if (!pointer.hasChild(prop)) {
                        pointer.putToOne(prop, PropertyMapping.createToOne());
                    }
                    if (!pointer.getChild(prop).hasKey(nav)) {
                        if (i < navList.length - 1) {
                            pointer.getChild(prop).putKey(nav);
                        } else {
                            ColumnProperty columnProperty = new ColumnProperty(column);
                            columnProperty.setType(type);
                            columnProperty.setId(isId);
                            pointer.getChild(prop).put(nav, columnProperty);
                        }
                    }
                    pointer = pointer.getChild(prop);
                    prop = nav;
                }
            }
        }
        return propertyMapping.getChild("base");
    }

    private boolean hasKey(String key) {
        return keys.contains(key);
    }

    private void putKey(String key) {
        keys.add(key);
    }

    private String toString(int depth) {
        int indent = depth * 2;
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (hasColumnProperty(key)) {
                sb.append(" ".repeat(indent))
                        .append(key)
                        .append(": {");
                ColumnProperty columnProperty = getColumnProperty(key);
                List<String> props = new ArrayList<>();
                Optional.ofNullable(columnProperty.getColumn())
                        .ifPresent(column -> props.add("column: \"" + column +"\""));
                Optional.ofNullable(columnProperty.getType())
                        .ifPresent(type -> props.add("column: \"" + type +"\""));
                Optional.ofNullable(columnProperty.getDefaultValue())
                        .ifPresent(defaultValue -> props.add("column: \"" + defaultValue +"\""));
                sb.append(String.join(", ", props)).append("},\n");
            }
            if (toOneProperties.containsKey(key)) {
                PropertyMapping propertyMapping = toOneProperties.get(key);
                sb.append(" ".repeat(indent)).append(key).append(": {\n")
                        .append(propertyMapping.toString(depth + 1))
                        .append(" ".repeat(indent)).append("},\n");
            }
            if (toManyProperties.containsKey(key)) {
                PropertyMapping propertyMapping = toManyProperties.get(key);
                sb.append(" ".repeat(indent)).append(key).append(": {[\n")
                        .append(propertyMapping.toString(depth + 1))
                        .append(" ".repeat(indent)).append("]},\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(0);
    }
}
