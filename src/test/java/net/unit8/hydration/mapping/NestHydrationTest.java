package net.unit8.hydration.mapping;

import net.unit8.hydration.NestHydration;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class NestHydrationTest {
    @Test
    void test() {
        NestHydration nestHydration = new NestHydration();
        Object nest = nestHydration.nest(List.of(
                new LinkedHashMap<String, Object>() {
                    {
                        put("_a", 1);
                        put("_b__c", "c1");
                    }
                },
                new LinkedHashMap<String, Object>() {
                    {
                        put("_a", 1);
                        put("_b__c", "c2");
                    }
                },
                new LinkedHashMap<String, Object>() {
                    {
                        put("_a", 2);
                        put("_b__c", "c3");
                    }
                },
                new LinkedHashMap<String, Object>() {
                    {
                        put("_a", 2);
                        put("_b__c", "c4");
                    }
                }));
    }
}
