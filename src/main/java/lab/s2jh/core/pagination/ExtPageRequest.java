package lab.s2jh.core.pagination;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Spring Data tab expand the definition of objects, add precise offset control properties .
 * @see {@link PropertyFilter#buildPageableFromHttpRequest(javax.servlet.http.HttpServletRequest)}
 */
public class ExtPageRequest extends PageRequest {

    private static final long serialVersionUID = 7944779254954509445L;

    private int offset = -1;

    public ExtPageRequest(int page, int size, Sort sort) {
        super(page, size, sort);
    }

    public ExtPageRequest(int offset, int page, int size, Sort sort) {
        super(page, size, sort);
        this.offset = offset;
    }

    public int getOffset() {
        if (offset > -1) {
            return offset;
        } else {
            return super.getOffset();
        }
    }

    /**
     * Usually there is no tab for the collection of data is converted to the corresponding assembly Page object passed to the Grid component to the front end of a unified structure of JSON data
     * @param List generic collection data
     * @return Conversion package Page tab structure objects
     */
    public static <S> Page<S> buildPageResultFromList(List<S> list) {
        Page<S> page = new PageImpl<S>(list);
        return page;
    }
}
