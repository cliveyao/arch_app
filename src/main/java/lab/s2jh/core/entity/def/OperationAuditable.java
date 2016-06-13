package lab.s2jh.core.entity.def;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public interface OperationAuditable {

    /**
     * Data conversion operation status display string literal value
     * @return
     */
    public abstract String convertStateToDisplay(String rawState);

}
