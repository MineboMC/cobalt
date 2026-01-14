package net.minebo.cobalt.util.pagination;

import java.util.*;
import java.util.stream.Collectors;

public class PaginatedResult<T> {

    private final LinkedHashMap<String, T> ranked;
    private final int resultsPerPage;

    // Without comparator
    public PaginatedResult(HashMap<String, T> input, int resultsPerPage) {
        this.ranked = input.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        this.resultsPerPage = resultsPerPage;
    }

    // With comparator (should look like this somewhat (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())
    public PaginatedResult(HashMap<String, T> input, int resultsPerPage, Comparator<Map.Entry<String, T>> comparator) {
        this.ranked = input.entrySet()
                .stream()
                .sorted(comparator)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        this.resultsPerPage = resultsPerPage;
    }

    public LinkedHashMap<String, T> getPage(int page) {
        int startIndex = (page - 1) * resultsPerPage;
        return ranked.entrySet()
                .stream()
                .skip(startIndex)
                .limit(resultsPerPage)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public boolean hasMultiplePages() {
        return ranked.size() > resultsPerPage;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) ranked.size() / resultsPerPage);
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public LinkedHashMap<String, T> getRanked() {
        return ranked;
    }
}
