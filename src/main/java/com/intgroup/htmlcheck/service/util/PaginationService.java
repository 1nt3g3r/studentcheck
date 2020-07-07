package com.intgroup.htmlcheck.service.util;

import org.springframework.stereotype.Service;

@Service
public class PaginationService {
    public static final int PAGE_SIZE = 50;

    public int getPageCount(int recordCount) {
        int lastPageElementCount = recordCount%PAGE_SIZE;

        int pageCount = recordCount / PAGE_SIZE;
        if (lastPageElementCount > 0) {
            pageCount++;
        }

        return pageCount;
    }

    public int getMinPage(int recordCount) {
        return 0;
    }

    public int getMaxPage(int recordCount) {
        int result = getPageCount(recordCount) - 1;

        if (result < 0) {
            result = 0;
        }

        return result;
    }

    public int normalizePageIndex(int pageIndex, int pageCount) {
        pageIndex = Math.min(pageIndex, pageCount - 1);
        if (pageIndex < 0) {
            pageIndex = 0;
        }
        return pageIndex;
    }
}
