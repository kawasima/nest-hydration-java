package net.unit8.hydration.mapping;

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
        PropertyMapping propertyMapping = PropertyMapping.structPropToColumnMapFromColumnHints(List.of(
                "a"
        ));
        assertThat(propertyMapping).isNotNull();
        assertThat(propertyMapping.getColumnProperty("a")).isNotNull();
        assertThat(propertyMapping.getColumnProperty("a").getColumn()).isEqualTo("a");
    }

    @Test
    void passedComplexSingleBaseScenarioAsColumnListWithNumberSpecifiers() {
        PropertyMapping propertyMapping = PropertyMapping.structPropToColumnMapFromColumnHints(List.of(
                "id___NUMBER",
                "a_id___NUMBER",
                "a_b",
                "a_c__id___NUMBER",
                "a_c__d",
                "a_e_id___NUMBER",
                "a_e_f"
        ));

        assertThat(propertyMapping).isNotNull();
        assertThat(propertyMapping.getColumnProperty("id")).isNotNull();
    }
}