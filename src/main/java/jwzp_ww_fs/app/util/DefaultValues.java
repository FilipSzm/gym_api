package jwzp_ww_fs.app.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Configuration
public class DefaultValues {

    public final Pageable defaultPageable;

    @Autowired
    public DefaultValues(@Value("${spring.data.web.pageable.default-page-size}") int defaultSize) {
        this.defaultPageable = PageRequest.of(0, defaultSize, Sort.unsorted());
    }
}
