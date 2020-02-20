package net.unit8.hydration;

import net.unit8.hydration.mapping.ColumnProperty;
import net.unit8.hydration.mapping.ToOneProperty;
import net.unit8.hydration.mapping.ValueProperty;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

public class ObjectMeta implements Serializable {
    private List<ValueProperty> valueList;
    private List<ToOneProperty> toOneList;
    private List<String> toManyPropList;
    private String containingColumn;
    private String ownProp;
    private boolean isOneOfMany;
    private Map<String, Map<String, Object>> cache;
    private Map<Object, Set<Object>> containingIdUsage;
    private Object defaultValue;

    public ObjectMeta(List<ValueProperty> valueList, List<ToOneProperty> toOneList, List<String> toManyPropList, String containingColumn, String ownProp, boolean isOneOfMany, Object defaultValue) {
        this.valueList = valueList;
        this.toOneList = toOneList;
        this.toManyPropList = toManyPropList;
        this.containingColumn = containingColumn;
        this.ownProp = ownProp;
        this.isOneOfMany = isOneOfMany;
        this.cache = new HashMap<>();
        this.containingIdUsage = new HashMap<>();
        this.defaultValue = defaultValue;
    }

    public Stream<ValueProperty> valuePropertyStream() {
        return valueList.stream();
    }

    public boolean hasCache(Object value) {
        return cache.containsKey(value);
    }

    public Map<String, Object> retrieveCache(Object value) {
        return cache.get(value);
    }

    public Stream<String> toManyPropStream() {
        return toManyPropList.stream();
    }

    public Stream<ToOneProperty> toOnePropStream() {
        return toOneList.stream();
    }

    public boolean isOneOfMany() {
        return isOneOfMany;
    }

    public String getContainingColumn() {
        return containingColumn;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getOwnProp() {
        return ownProp;
    }

    public boolean isContainingIdUsage(Object value, Object containingId) {
        return containingIdUsage.containsKey(value) && containingIdUsage.get(value).contains(containingId);
    }

    public boolean hasContainingIdUsage() {
        return containingIdUsage != null;
    }

    public void putContainingIdUsage(Object value, Object containingId) {
        if (!containingIdUsage.containsKey(value)) {
            containingIdUsage.put(value, new HashSet<>());
        }
        containingIdUsage.get(value).add(containingId);
    }
}
