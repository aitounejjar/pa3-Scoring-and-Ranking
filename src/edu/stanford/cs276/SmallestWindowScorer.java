package edu.stanford.cs276;

import edu.stanford.cs276.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A skeleton for implementing the Smallest Window scorer in Task 3.
 * Note: The class provided in the skeleton code extends BM25Scorer in Task 2. However, you don't necessarily
 * have to use Task 2. (You could also use Task 1, in which case, you'd probably like to extend CosineSimilarityScorer instead.)
 * Also, feel free to modify or add helpers inside this class.
 */
public class SmallestWindowScorer extends CosineSimilarityScorer {

    private static final double BOOST_FACTOR = 1.2;
    double urlweight            = 11;
    double titleweight          = 10;
    double headerweight         = 99;
    double anchorweight         = 8;
    double bodyweight           = 9;
    double smoothingBodyLength  = 1000;

    public SmallestWindowScorer(Map<String, Double> idfs, Map<Query, Map<String, Document>> queryDict) {
        super(idfs);
    }

    /**
     * get smallest window of one document and query pair.
     *
     * @param d: document
     * @param q: query
     */
    private int getWindow(Document d, Query q) {
    /*
     * @//TODO : Your code here
     */

        List<Integer> windows = new ArrayList<>();
        windows.add(Integer.MAX_VALUE);

        // compute smallest window for the body
        if (d.body_hits != null) {
            List<List<Integer>> positions = new ArrayList<>(d.body_hits.values());
            if (positions.size() == q.queryWords.size()) {
                // all query words appear in the body
                int window = computeSmallestWindow(positions);
                windows.add(window);
            }
        }

        // compute smallest window for url
        windows.add(computeSmallestWindow(d.url, q));

        // compute smallest window for title
        windows.add(computeSmallestWindow(d.title, q));

        // compute smallest window for headers
        if (d.headers != null) {
            for (String header : d.headers) {
                windows.add(computeSmallestWindow(header, q));
            }
        }

        // compute smallest window for anchors
        if (d.anchors != null) {
            for (String header : d.anchors.keySet()) {
                windows.add(computeSmallestWindow(header, q));
            }
        }

        Collections.sort(windows);

        return windows.get(0);
    }


    /**
     * get boost score of one document and query pair.
     *
     * @param d: document
     * @param q: query
     */
    private double getBoostScore(Document d, Query q) {

        /*
         * @//TODO : Your code here, calculate the boost score.
         *
         */

        int smallestWindow = getWindow(d, q);

        double boostScore;

        if (smallestWindow == q.queryWords.size()) {
            boostScore = BOOST_FACTOR;
        } else if (smallestWindow == Integer.MAX_VALUE) {
            boostScore = 1;
        } else {
            if(smallestWindow>50){
                boostScore = 1;
                //boostScore = BOOST_FACTOR - getBoostScore_helper(smallestWindow)/q.queryWords.size();
            }else {

                boostScore = BOOST_FACTOR - getBoostScore_helper(smallestWindow)/q.queryWords.size();
            }
        }

        return boostScore;
    }

    private double getBoostScore_helper(int smallestWindow) {
        return 1.0 / (smallestWindow);
    }

    @Override
    public double getSimScore(Document d, Query q) {
        Map<String, Map<String, Double>> tfs = this.getDocTermFreqs(d, q);
        this.normalizeTFs(tfs, d, q);
        Map<String, Double> tfQuery = getQueryFreqs(q);
        double boost = getBoostScore(d, q);
        double rawScore = this.getNetScore(tfs, q, tfQuery, d);
        return boost * rawScore;
    }

    private int computeSmallestWindow(String string, Query q) {
        int smallestWindow = Integer.MAX_VALUE;
        int count = 0;

        for (String w : q.queryWords) {
            if (!string.contains(w)) {
                return smallestWindow;
            }
        }

        // all query words appear in the passed strings
        String[] tokens = string.split("\\W+");
        Map<String, List<Integer>> positions = new HashMap<>();
        for (int i=0; i<tokens.length; ++i) {
            String token = tokens[i];
            if (!positions.containsKey(token)) {
                positions.put(token, new ArrayList<>());
            }
            positions.get(token).add(i);
        }

        return -1;
    }

    private int computeSmallestWindow(List<List<Integer>> positions) {

        // pre-processing: put a mapping from the minimums to the list which they came from
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (List<Integer> list : positions) {
            Set<Integer> set = new HashSet<>(list);
            list = new ArrayList<>(set);
            if (set.size() != list.size()){
                Assert.check(set.size() == list.size(), "List of positions contained duplicated. List Size: "
                        + list.size() + ", Set Size: " + set.size() + "\n" + list.toString());
            }

            Collections.sort(list);
            int min = list.get(0);
            map.put(min, list);
        }

        int smallestWindow = computeSmallestWindow_helper(map.keySet());

        while (true) {
            if (smallestWindow == positions.size() || allListsTraversed(map)) {
                break;
            }

            // find the minimum whose list can be increased
            List<Integer> mins = new ArrayList<>(map.keySet());
            Collections.sort(mins);
            for (int i=0; i<mins.size(); ++i) {
                // get the current minimum
                int currentMin = mins.get(i);
                // get the list where it came from
                List<Integer> list = map.get(currentMin);
                // get the currentMin's index in its list
                int index = list.indexOf(currentMin);
                if (index < list.size() - 1) {
                    int newMin = list.get(index+1);
                    map.remove(currentMin);
                    map.put(newMin, list);
                }
            }

            // by now, a new minimum has already been put to the map
            // we just need to compute the smallest window again

            int possibleSmallestWindow = computeSmallestWindow_helper(map.keySet());

            if (possibleSmallestWindow < smallestWindow) {
                smallestWindow = possibleSmallestWindow;
            }

        }

        return smallestWindow;

    }

    private int computeSmallestWindow_helper(Set<Integer> set) {
        int size = set.size();
        List<Integer> list = new ArrayList<>(set);
        Collections.sort(list);
        int range = list.get(size-1) - list.get(0) + 1;
        return range;
    }

    private boolean allListsTraversed(Map<Integer, List<Integer>> map) {

        boolean result = true;

        for (int minimum : map.keySet()) {
            List<Integer> list = map.get(minimum);
            int index = list.indexOf(minimum);
            if (index < list.size() - 1) {
                result = false;
                break;
            }
        }

        return result;
    }

}
