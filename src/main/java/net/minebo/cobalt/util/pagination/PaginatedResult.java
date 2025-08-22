package net.minebo.cobalt.util.pagination;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PaginatedResult {

    private final LinkedHashMap<String, Integer> ranked;
    private final int resultsPerPage;

    public PaginatedResult(HashMap<String, Integer> input, int resultsPerPage) {
        this.ranked = input.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        this.resultsPerPage = resultsPerPage;
    }

    public LinkedHashMap<String, Integer> getPage(int page) {
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

    public LinkedHashMap<String, Integer> getRanked() {
        return ranked;
    }

}
