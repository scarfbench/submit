package org.eclipse.pathfinder.api;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.eclipse.pathfinder.internal.GraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/graph-traversal")
public class GraphTraversalService {

    private static final long ONE_MIN_MS = 1000 * 60;
    private static final long ONE_DAY_MS = ONE_MIN_MS * 60 * 24;
    private final Random random = new Random();

    @Autowired
    private GraphDao dao;

    @GetMapping(value = "/shortest-path", produces = "application/json")
    public TransitPaths findShortestPath(
            @RequestParam("origin") String originUnLocode,
            @RequestParam("destination") String destinationUnLocode,
            @RequestParam(value = "deadline", required = false) String deadline) {

        List<String> allVertices = dao.listLocations();
        allVertices.remove(originUnLocode);
        allVertices.remove(destinationUnLocode);

        int candidateCount = getRandomNumberOfCandidates();
        List<TransitPath> candidates = new ArrayList<>(candidateCount);

        for (int i = 0; i < candidateCount; i++) {
            allVertices = getRandomChunkOfLocations(allVertices);
            List<TransitEdge> transitEdges = new ArrayList<>(allVertices.size() - 1);
            String fromUnLocode = originUnLocode;
            LocalDateTime date = LocalDateTime.now();

            for (int j = 0; j <= allVertices.size(); ++j) {
                LocalDateTime fromDate = nextDate(date);
                LocalDateTime toDate = nextDate(fromDate);
                String toUnLocode = (j >= allVertices.size() ? destinationUnLocode : allVertices.get(j));
                transitEdges.add(
                        new TransitEdge(
                                dao.getVoyageNumber(fromUnLocode, toUnLocode),
                                fromUnLocode, toUnLocode, fromDate, toDate));
                fromUnLocode = toUnLocode;
                date = nextDate(toDate);
            }
            candidates.add(new TransitPath(transitEdges));
        }
        return new TransitPaths(candidates);
    }

    private LocalDateTime nextDate(LocalDateTime date) {
        return date.plus(ONE_DAY_MS + (random.nextInt(1000) - 500) * ONE_MIN_MS, ChronoUnit.MILLIS);
    }

    private int getRandomNumberOfCandidates() {
        return 3 + random.nextInt(3);
    }

    private List<String> getRandomChunkOfLocations(List<String> allLocations) {
        Collections.shuffle(allLocations);
        int total = allLocations.size();
        int chunk = total > 4 ? 1 + random.nextInt(5) : total;
        return allLocations.subList(0, chunk);
    }
}
