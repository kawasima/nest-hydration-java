package net.unit8.hydration;

import net.unit8.hydration.mapping.PropertyMapping;

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

    public Object nest(List<Map<String, Object>> data, PropertyMapping structPropToColumnMap) {
        if (data == null) {
            return null;
        }

        if (isNull(structPropToColumnMap) && !data.isEmpty()) {
            structPropToColumnMap = PropertyMapping.structPropToColumnMapFromColumnHints(data.get(0).keySet().stream().collect(Collectors.toList()));
        }

        if (isNull(structPropToColumnMap)) {
            return null;
        } else if (data.isEmpty()) {
            return null;
        }

        NestStruct struct = new NestStruct();
        final HydrationMeta meta = new HydrationMeta(structPropToColumnMap);
        for (Map<String, Object> row : data) {
            meta.primeIdStream()
                    .forEach(primeIdColumn -> _nest(struct, row, primeIdColumn, meta));
        }
        return struct.getObject();
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
            objMeta.putCache(value, obj);
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
}
