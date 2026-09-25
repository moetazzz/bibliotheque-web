package bibliotheque.util;

import java.util.List;

/**
 * Encapsule les infos de pagination pour les vues.
 */
public class PaginationInfo<T> {

    private final List<T> items;
    private final int currentPage;    // 1-based
    private final int totalPages;
    private final int totalItems;
    private final int pageSize;

    public PaginationInfo(List<T> items, int currentPage, int totalPages,
                          int totalItems, int pageSize) {
        this.items = items;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
    }

    public List<T> getItems()       { return items; }
    public int getCurrentPage()     { return currentPage; }
    public int getTotalPages()      { return totalPages; }
    public int getTotalItems()      { return totalItems; }
    public int getPageSize()        { return pageSize; }

    public boolean hasPrevious()    { return currentPage > 1; }
    public boolean hasNext()        { return currentPage < totalPages; }
    public int getPreviousPage()    { return Math.max(1, currentPage - 1); }
    public int getNextPage()        { return Math.min(totalPages, currentPage + 1); }
    public boolean isEmpty()        { return items.isEmpty(); }

    /** Génère la liste des pages à afficher (ex: 1, 2, ..., 5, 6, 7, ..., 12) */
    public List<Integer> getPageNumbers() {
        List<Integer> pages = new java.util.ArrayList<>();
        if (totalPages <= 7) {
            for (int i = 1; i <= totalPages; i++) pages.add(i);
        } else {
            pages.add(1);
            int start = Math.max(2, currentPage - 1);
            int end = Math.min(totalPages - 1, currentPage + 1);
            if (start > 2) pages.add(-1); // ellipsis
            for (int i = start; i <= end; i++) pages.add(i);
            if (end < totalPages - 1) pages.add(-1);
            pages.add(totalPages);
        }
        return pages;
    }
}