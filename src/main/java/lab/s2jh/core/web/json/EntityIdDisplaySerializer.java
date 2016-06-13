package lab.s2jh.core.web.json;

import java.io.IOException;
import java.io.Serializable;

import lab.s2jh.core.entity.PersistableEntity;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * label-value in the form of data analysis is generally associated with an object list for display output
 *
 */
public class EntityIdDisplaySerializer extends JsonSerializer<PersistableEntity<? extends Serializable>> {

    @Override
    public void serialize(PersistableEntity<? extends Serializable> value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value != null) {
            jgen.writeStartObject();
            jgen.writeFieldName("id");
            jgen.writeString(ObjectUtils.toString(value.getId()));
            jgen.writeFieldName("display");
            jgen.writeString(value.getDisplay());
            jgen.writeEndObject();
        }
    }
}
