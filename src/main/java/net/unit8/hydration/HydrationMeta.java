package net.unit8.hydration;

import net.unit8.hydration.mapping.PropertyMapping;
import net.unit8.hydration.mapping.ToOneProperty;
import net.unit8.hydration.mapping.ValueProperty;

import java.util.*;
import java.util.stream.Stream;

public class HydrationMeta {
    private List<String> primeIdColumnList;
    private Map<String, ObjectMeta> idMap;

    public HydrationMeta(PropertyMapping propertyMapping) {
        this.primeIdColumnList = new ArrayList<>();
        this.idMap = new HashMap<>();

        if (propertyMapping.isOneToMany()) {
            buildMeta(propertyMapping, true, null, null);
        } else {
            if (!propertyMapping.hasColumnProperty(propertyMapping.keys().get(0))) {
                throw new IllegalArgumentException("invalid structPropToColumnMap format - the base object can not be an empty object");
            }
            addPrimeIdColumn(propertyMapping.getColumnProperty(propertyMapping.keys().get(0)).getColumn());
            buildMeta(propertyMapping, false, null, null);
        }
    }

    public void addPrimeIdColumn(String idColumn) {
        primeIdColumnList.add(idColumn);
    }

    public void putToIdMap(String idColumn, ObjectMeta objectMeta) {
        idMap.put(idColumn, objectMeta);
    }

    public Stream<String> primeIdStream() {
        return primeIdColumnList.stream();
    }

    public ObjectMeta getObjectMeta(String idColumn) {
        return idMap.get(idColumn);
    }

    private void buildMeta(PropertyMapping structPropToColumnMap,
                            boolean isOneOfMany,
                            String containingColumn,
                            String ownProp) {
        List<String> propList = structPropToColumnMap.keys();

        if (propList.isEmpty()) {
            throw new IllegalArgumentException("invalid structPropToColumnMap format - property '" + ownProp + "' can not be an empty array");
        }

        String idProp = propList.stream()
                .filter(prop -> structPropToColumnMap.hasColumnProperty(prop))
                .filter(prop -> structPropToColumnMap.getColumnProperty(prop).isId())
                .findFirst()
                .orElse(propList.get(0));

        String idColumn = structPropToColumnMap.getColumnProperty(idProp).getColumn();

        if (isOneOfMany) {
            addPrimeIdColumn(idColumn);
        }
        List<ValueProperty> valueList = new ArrayList<>();
        List<String> toManyPropList = new ArrayList<>();
        List<ToOneProperty> toOnePropList = new ArrayList<>();

        for (String prop : propList) {
            if (structPropToColumnMap.hasColumnProperty(prop)) {
                valueList.add(new ValueProperty(
                        structPropToColumnMap.getColumnProperty(prop),
                        prop));
            } else if (structPropToColumnMap.hasToMany(prop)) {
                toManyPropList.add(prop);
                buildMeta(structPropToColumnMap.getToMany(prop), true, idColumn, prop);
            } else if (structPropToColumnMap.hasToOne(prop)) {
                String subIdColumn = structPropToColumnMap.getToOne(prop).keys().get(0);
                toOnePropList.add(new ToOneProperty(prop, subIdColumn));
                buildMeta(structPropToColumnMap.getToOne(prop), false, idColumn, prop);
            }
        }
        putToIdMap(idColumn, new ObjectMeta(
                valueList,
                toOnePropList,
                toManyPropList,
                containingColumn,
                ownProp,
                isOneOfMany,
                structPropToColumnMap.getColumnProperty(idProp).getDefaultValue()));
    }
}
