package edu.stanford.cs276;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skeleton code for the implementation of a BM25 Scorer in Task 2.
 */
public class BM25Scorer extends AScorer {

    /*
     *  TODO: You will want to tune these values
     */
    double urlweight = 0.1;
    double titleweight = 0.1;
    double bodyweight = 0.1;
    double headerweight = 0.1;
    double anchorweight = 0.1;

    // BM25-specific weights
    double burl = 0.1;
    double btitle = 0.1;
    double bheader = 0.1;
    double bbody = 0.1;
    double banchor = 0.1;

    double k1 = 0.1;
    double pageRankLambda = 0.1;
    double pageRankLambdaPrime = 0.1;

    // query -> url -> document
    Map<Query, Map<String, Document>> queryDict;

    /* BM25 data structures--feel free to modify these */

    // Document -> field -> length
    Map<Document, Map<String, Double>> lengths;

    // field name -> average length
    Map<String, Double> avgLengths;

    // Document -> pagerank score
    Map<Document, Double> pagerankScores;

    /**
     * Construct a BM25Scorer.
     *
     * @param idfs      the map of idf scores
     * @param queryDict a map of query to url to document
     */
    public BM25Scorer(Map<String, Double> idfs, Map<Query, Map<String, Document>> queryDict) {
        super(idfs);
        this.queryDict = queryDict;
        this.calcAverageLengths();
    }

    /**
     * Set up average lengths for BM25, also handling PageRank.
     */
    public void calcAverageLengths() {
        lengths = new HashMap<>();
        avgLengths = new HashMap<>();
        pagerankScores = new HashMap<>();

        /*
         * TODO : Your code here
         * Initialize any data structures needed, perform
         * any preprocessing you would like to do on the fields,
         * handle pagerank, accumulate lengths of fields in documents.
         */

        int bodyCount   = 0, bodyWordsCount   = 0;
        int headerCount = 0, headerWordsCount = 0;
        int anchorCount = 0, anchorWordsCount = 0;
        int titleCount  = 0, titleWordsCount  = 0;
        int urlCount    = 0, urlWordsCount    = 0;

        for (Query query : queryDict.keySet()) {
            Map<String, Document> map = queryDict.get(query);
            for (String url : map.keySet()) {
                Document document = map.get(url);
                if (lengths.containsKey(document)) {
                    continue; // this document has already been parsed
                }

                // page rank
                pagerankScores.put(document, (double)document.page_rank);

                // body
                bodyWordsCount += document.body_length;
                bodyCount++;
                lengths.put(document, new HashMap<>());
                lengths.get(document).put("body", (double)document.body_length);

                // header
                List<String> headers = document.headers;
                if (headers != null) {
                    int numHeaders = 0; // num of headers in the current document
                    for (String header : headers) {
                        String[] matches = header.split("\\s+");
                        headerWordsCount += matches.length;
                        numHeaders += matches.length;
                        headerCount++;
                    }
                    lengths.get(document).put("header", (double)numHeaders);
                }

                // anchor
                Map<String, Integer> anchors = document.anchors;
                if (anchors != null) {
                    int numAnchors = 0; // num of anchors in the current document
                    for (String anchor : anchors.keySet()) {
                        String[] matches = anchor.split("\\s+");
                        anchorWordsCount += matches.length;
                        numAnchors += matches.length;
                        anchorCount++;
                    }
                    lengths.get(document).put("anchor", (double)numAnchors);
                }

                // title
                if (document.title != null) {
                    String[] matches = document.title.split("\\s+");
                    titleWordsCount += matches.length;
                    titleCount++;
                    lengths.get(document).put("title", (double)matches.length);
                }

                // url
                if (document.url != null) {
                    String[] matches = document.url.split("\\W+");
                    urlWordsCount += matches.length;
                    urlCount++;
                    lengths.get(document).put("url", (double)matches.length);
                }
            }
        }

        // compute averages
        avgLengths.put("body",   bodyWordsCount/(double)bodyCount);
        avgLengths.put("header", headerWordsCount/(double)headerCount);
        avgLengths.put("anchor", anchorWordsCount/(double)anchorCount);
        avgLengths.put("title",  titleWordsCount/(double)titleCount);
        avgLengths.put("url",    urlWordsCount/(double)urlCount);

        for (String tfType : this.TFTYPES) {
            /*
             * TODO : Your code here
             * Normalize lengths to get average lengths for
             * each field (body, url, title, header, anchor)
             */
        }

    }

    /**
     * Get the net score.
     *
     * @param tfs     the term frequencies
     * @param q       the Query
     * @param tfQuery
     * @param d       the Document
     * @return the net score
     */
    public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String, Double> tfQuery, Document d) {

        double score = 0.0;
    
        /*
         * TODO : Your code here
         * Use equation 5 in the writeup to compute the overall score
         * of a document d for a query q.
         */

        return score;
    }

    /**
     * Do BM25 Normalization.
     *
     * @param tfs the term frequencies
     * @param d   the Document
     * @param q   the Query
     */
    public void normalizeTFs(Map<String, Map<String, Double>> tfs, Document d, Query q) {

      /*
       * TODO : Your code here
       * Use equation 3 in the writeup to normalize the raw term frequencies
       * in fields in document d.
       */

        for (String section : tfs.keySet()) {
            // query word -> num of its occurrences in the section
            Map<String, Double> map = tfs.get(section);
            double bWeight = getSectionBWeight(section);
            for (String queryWord : map.keySet()) {

                double numOccurrences = map.get(queryWord);

                // length of the section in the document
                double length = lengths.get(d).get(section);

                // average length for the section
                double averageLength = avgLengths.get(section);

                // field dependant normalized term frequency
                double fdtf = numOccurrences / (1 + (bWeight * ( (length / averageLength) - 1 )));

                tfs.get(section).put(queryWord, fdtf);

            }

        }



    }

    /**
     * Write the tuned parameters of BM25 to file.
     * Only used for grading purpose, you should NOT modify this method.
     *
     * @param filePath the output file path.
     */
    private void writeParaValues(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            String[] names = {
                    "urlweight", "titleweight", "bodyweight",
                    "headerweight", "anchorweight", "burl", "btitle",
                    "bheader", "bbody", "banchor", "k1", "pageRankLambda", "pageRankLambdaPrime"
            };
            double[] values = {
                    this.urlweight, this.titleweight, this.bodyweight,
                    this.headerweight, this.anchorweight, this.burl, this.btitle,
                    this.bheader, this.bbody, this.banchor, this.k1, this.pageRankLambda,
                    this.pageRankLambdaPrime
            };
            BufferedWriter bw = new BufferedWriter(fw);
            for (int idx = 0; idx < names.length; ++idx) {
                bw.write(names[idx] + " " + values[idx]);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    /**
     * Get the similarity score.
     * @param d the Document
     * @param q the Query
     * @return the similarity score
     */
    public double getSimScore(Document d, Query q) {
        Map<String, Map<String, Double>> tfs = this.getDocTermFreqs(d, q);
        this.normalizeTFs(tfs, d, q);
        Map<String, Double> tfQuery = getQueryFreqs(q);

        // Write out the tuned BM25 parameters
        // This is only used for grading purposes.
        // You should NOT modify the writeParaValues method.
        writeParaValues("bm25Para.txt");
        return getNetScore(tfs, q, tfQuery, d);
    }

    private double getSectionBWeight(String section) {
        double sectionBWeight;
        switch (section) {
            case "url"      : sectionBWeight = burl;    break;
            case "title"    : sectionBWeight = btitle;  break;
            case "body"     : sectionBWeight = bbody;   break;
            case "header"   : sectionBWeight = bheader; break;
            case "anchor"   : sectionBWeight = banchor; break;
            default         : throw new RuntimeException("Illegal section type of '" + section + "' was found in the tfs map.");
        }
        return sectionBWeight;
    }

}
