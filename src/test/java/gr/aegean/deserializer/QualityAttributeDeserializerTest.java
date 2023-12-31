package gr.aegean.deserializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;

import gr.aegean.model.analysis.quality.QualityAttribute;


class QualityAttributeDeserializerTest {
    private QualityAttributeDeserializer underTest;

    @BeforeEach
    void setup() {
        underTest = new QualityAttributeDeserializer();
    }

    @ParameterizedTest
    @ValueSource(strings = {"HOTSPOT_PRIORITY", "hotspot_PRIORITY", "  Hotspot  Priority  "})
    void shouldDeserializeQualityAttributeIgnoringCaseAndSpaces(String attribute) throws IOException {
        // Arrange
        QualityAttribute expected = QualityAttribute.HOTSPOT_PRIORITY;
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(attribute);

        // Act
        QualityAttribute actual = underTest.deserialize(parser, context);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }
    @Test
    void shouldDeserializeQualityAttributeIgnoringCaseAndExtraSpaces() throws IOException {
        // Arrange
        String input = "  Bug  Severity  ";
        String expected = "BUG_SEVERITY";
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(input);

        // Act
        QualityAttribute actual = underTest.deserialize(parser, context);

        // Assert
        assertThat(actual).isEqualTo(QualityAttribute.valueOf(expected));
    }

    /*
        Null case is covered by the @Valid annotation. There will be no mapping if the quality attribute is null so no
        deserialization will happen for the quality attribute property.
     */
    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"invalidAttribute"})
    void shouldThrowIllegalArgumentExceptionWhenQualityAttributeIsInvalid(String attribute) throws IOException {
        // Arrange
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        // Act
        when(parser.getValueAsString()).thenReturn(attribute);

        // Assert
        assertThatThrownBy(() -> underTest.deserialize(parser, context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid quality attribute: " + attribute.toUpperCase());
    }
}
