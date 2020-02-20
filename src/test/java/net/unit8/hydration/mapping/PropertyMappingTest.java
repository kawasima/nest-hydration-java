package net.unit8.hydration.mapping;

import net.unit8.hydration.HydrationMeta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyMappingTest {

    @Test
    void passedEmptyAsColumnList() {
        assertThat(PropertyMapping.structPropToColumnMapFromColumnHints(List.of()))
                .isNull();
    }

    @Test
    void passedSingleDirectPropertyAsColumnList() {
        assertThat(PropertyMapping.structPropToColumnMapFromColumnHints(List.of(
                "a"
        )));
    }

    @Test
    void passedComplexSingleBaseScenarioAsColumnListWithNumberSpecifiers() {
        assertThat(PropertyMapping.structPropToColumnMapFromColumnHints(List.of(
                "id___NUMBER",
                "a_id___NUMBER",
                "a_b",
                "a_c__id___NUMBER",
                "a_c__d",
                "a_e_id___NUMBER",
                "a_e_f"
        ))).isNull();
    }

    @Test
    void test() {
        PropertyMapping propertyMapping = PropertyMapping.structPropToColumnMapFromColumnHints(List.of(
                "id___NUMBER",
                "a_id___NUMBER",
                "a_b",
                "a_c__id___NUMBER",
                "a_c__d",
                "a_e_id___NUMBER",
                "a_e_f"
        ));
        HydrationMeta hydrationMeta = new HydrationMeta(propertyMapping);
    }
}