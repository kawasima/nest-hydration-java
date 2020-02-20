package net.unit8.hydration;

import net.unit8.hydration.mapping.PropertyMapping;
import net.unit8.hydration.mapping.ToOneProperty;
import net.unit8.hydration.mapping.ValueProperty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class NestHydration {
    private Map<String, Function<Object, Object>> typeHandlers;

    public NestHydration() {
        typeHandlers = new HashMap<>();
        typeHandlers.put("NUMBER", cellValue -> Float.parseFloat(Objects.toString(cellValue)));
    }

    public Object nest(List<Map<String, Object>> data) {
        return nest(data, null);
    }

    public NestStruct nest(List<Map<String, Object>> data, PropertyMapping structPropToColumnMap) {
        if (data == null) {
            return null;
        }

        if (isNull(structPropToColumnMap)) {
            structPropToColumnMap = PropertyMapping.structPropToColumnMapFromColumnHints(data.get(0).keySet().stream().collect(Collectors.toList()));
        }

        NestStruct struct = new NestStruct();
        final HydrationMeta meta = new HydrationMeta(structPropToColumnMap);
        for (Map<String, Object> row : data) {
            meta.primeIdStream()
                    .forEach(primeIdColumn -> _nest(struct, row, primeIdColumn, meta));
        }
        return struct;
    }

    private void _nest(NestStruct struct,
                       Map<String, Object> row,
                       String idColumn,
                       HydrationMeta meta) {
        Object value = row.get(idColumn);
        ObjectMeta objMeta = meta.getObjectMeta(idColumn);

        if (isNull(value)) {
            value = objMeta.getDefaultValue();
            if (value == null) return;
        }
        
        Map<String, Object> obj;
        if (objMeta.hasCache(value)) {
            if (!objMeta.hasContainingIdUsage()) {
                return;
            }
            Object containingId = row.get(objMeta.getContainingColumn());
            if (objMeta.isContainingIdUsage(value, containingId)) {
                return;
            }
            obj = objMeta.retrieveCache(value);
        } else {
            obj = objMeta.valuePropertyStream()
                    .map(valueProperty -> {
                        Object cellValue = row.get(valueProperty.getColumn());
                        if (nonNull(cellValue)) {
                            // TODO TypeHandler
                        } else if (valueProperty.hasDefaultValue()) {
                            cellValue = valueProperty.getDefaultValue();
                        }
                        return new AbstractMap.SimpleImmutableEntry<String, Object>(valueProperty.getProp(), cellValue);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            objMeta.toManyPropStream()
                    .forEach(prop -> obj.put(prop, new ArrayList<>()));

            objMeta.toOnePropStream()
                    .forEach(toOne -> {
                        obj.put(toOne.getProp(), null);
                        _nest(struct, row, toOne.getColumn(), meta);
                    });
        }

        if (objMeta.getContainingColumn() == null) {
            if (objMeta.isOneOfMany()) {
                if (struct.isEmpty()) {
                    struct.initializeAsList();
                }
                struct.add(obj);
            } else {
                struct.set(obj);
            }
        } else {
            Object containingId = row.get(objMeta.getContainingColumn());
            Map<String, Object> container = meta.getObjectMeta(objMeta.getContainingColumn()).retrieveCache(containingId);

            if (nonNull(container)) {
                if (objMeta.isOneOfMany()) {
                    ((List<Map<String, Object>>) container.get(objMeta.getOwnProp())).add(obj);
                } else {
                    container.put(objMeta.getOwnProp(), obj);
                }
            }

            objMeta.putContainingIdUsage(value, containingId);
        }
    }
    
    private void _buildMeta(HydrationMeta meta,
                            PropertyMapping structPropToColumnMap,
                            boolean isOneOfMany,
                            String containingColumn,
                            String ownProp) {
        List<String> propList = structPropToColumnMap.keys();

        if (propList.isEmpty()) {
            throw new IllegalArgumentException("invalid structPropToColumnMap format - property '" + ownProp + "' can not be an empty array");
        }

        String idProp = propList.stream()
                .filter(prop -> structPropToColumnMap.getColumnProperty(prop).isId())
                .findFirst()
                .orElse(propList.get(0));

        String idColumn = structPropToColumnMap.getColumnProperty(idProp).getColumn();

        if (isOneOfMany) {
            meta.addPrimeIdColumn(idColumn);
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
                _buildMeta(meta, structPropToColumnMap.getToMany(prop), true, idColumn, prop);
            } else if (structPropToColumnMap.hasToOne(prop)) {
                String subIdColumn = structPropToColumnMap.getToOne(prop).keys().get(0);
                toOnePropList.add(new ToOneProperty(prop, subIdColumn));
                _buildMeta(meta, structPropToColumnMap.getToOne(prop), false, idColumn, prop);
            }
        }
        meta.putToIdMap(idColumn, new ObjectMeta(
                valueList,
                toOnePropList,
                toManyPropList,
                containingColumn,
                ownProp,
                isOneOfMany,
                structPropToColumnMap.getColumnProperty(idProp).getDefaultValue()));
    }
}
