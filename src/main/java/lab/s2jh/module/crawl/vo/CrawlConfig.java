package lab.s2jh.module.crawl.vo;

import java.util.Date;

import lab.s2jh.core.annotation.MetaData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
@Accessors(chain = true)
public class CrawlConfig {

    private boolean forceRefetch = false;

    private boolean forceReparse = false;

    private Long batchId;

    @MetaData(value = "Concurrent crawls threads", comments = "For fast, no anti- reptile sites can be set according to a larger machine performance ; anyway, set to a smaller number of")
    private int threadNum = 30;

    @MetaData(value = "Crawl Access minimum interval ( in seconds )", comments = "Some sites do a certain anti- crawler control , such as restrictions on user request interval may not be too fast , through a reasonable set this parameter to circumvent the blockade site")
    private int fetchMinInterval = 0;

    private Date lastFetchTime;

    private Date startTime = new Date();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
