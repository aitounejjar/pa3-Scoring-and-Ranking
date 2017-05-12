package edu.stanford.cs276;

import edu.stanford.cs276.util.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Skeleton code for the implementation of a
 * Cosine Similarity Scorer in Task 1.
 */
public class CosineSimilarityScorer extends AScorer {

    /*
     * TODO: You will want to tune the values for
     * the weights for each field.
     */
    double urlweight = 0.1;
    double titleweight = 0.1;
    double bodyweight = 0.1;
    double headerweight = 0.1;
    double anchorweight = 0.1;
    double smoothingBodyLength = 1.0;

    /**
     * Construct a Cosine Similarity Scorer.
     *
     * @param idfs the map of idf values
     */
    public CosineSimilarityScorer(Map<String, Double> idfs) {
        super(idfs);
    }

    /**
     * Get the net score for a query and a document.
     *
     * @param tfs     the term frequencies
     * @param q       the Query
     * @param tfQuery the term frequencies for the query
     * @param d       the Document
     * @return the net score
     */
    public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String, Double> tfQuery, Document d) {
        double score = 0.0;
    
        /*
         * TODO : Your code here
         * See Equation 2 in the handout regarding the net score between a query vector and the term score
         * vectors for a document.
         *
         */


        for (String term : q.queryWords) {
            // compute the tf-idf weight of the term (weight = tf x idf)
            double tf = 1 + Math.log10(tfQuery.get(term));

            double idf = idfs.containsKey(term) ? idfs.get(term) : idfs.get(LoadHandler.UNSEEN_TERM_ID);
            double tfIdfWeight = tf * idf;

            // loop over sections: "url", "title", "body", "header", "anchor"
            double sectionScores = 0.0;
            for (String section : tfs.keySet()) {
                Map<String, Double> numbers = tfs.get(section);
                double sectionWeight;
                switch (section) {
                    case "url"      : sectionWeight = urlweight;    break;
                    case "title"    : sectionWeight = titleweight;  break;
                    case "body"     : sectionWeight = bodyweight;   break;
                    case "header"   : sectionWeight = headerweight; break;
                    case "anchor"   : sectionWeight = anchorweight; break;
                    default         : throw new RuntimeException("Illegal section type of '" + section + "' was found in the tfs map.");
                }

                double sectionScore = sectionWeight * (tfs.get(section).containsKey(term) ? tfs.get(section).get(term) : 0.0);
                sectionScores += sectionScore;
            } // end inner for loop

            // do the multiplication in equation (2)
            score += tfIdfWeight * sectionScores;

        } // end outer for loop


        return score;
    }

    /**
     * Normalize the term frequencies.
     *
     * @param tfs the term frequencies
     * @param d   the Document
     * @param q   the Query
     */
    public void normalizeTFs(Map<String, Map<String, Double>> tfs, Document d, Query q) {

        /*
         * TODO : Your code here
         * Note that we should give uniform normalization to all
         * fields as discussed in the assignment handout.
         */

        int bodyLengthSmoother = d.body_length + Config.BODY_LENGTH_SMOOTHING;

        for (String tfType : tfs.keySet()) {
            Map<String, Double> map = tfs.get(tfType);
            for (String w : map.keySet()) {
                double adjusted = map.get(w) / bodyLengthSmoother;
                map.put(w, adjusted);
            }
        }
    }

    /**
     * Write the tuned parameters of cosineSimilarity to file.
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
                    "urlweight", "titleweight", "bodyweight", "headerweight",
                    "anchorweight", "smoothingBodyLength"
            };
            double[] values = {
                    this.urlweight, this.titleweight, this.bodyweight,
                    this.headerweight, this.anchorweight, this.smoothingBodyLength
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
    /** Get the similarity score between a document and a query.
     * @param d the Document
     * @param q the Query
     * @return the similarity score.
     */
    public double getSimScore(Document d, Query q) {
        Map<String, Map<String, Double>> tfs = this.getDocTermFreqs(d, q);
        this.normalizeTFs(tfs, d, q);
        Map<String, Double> tfQuery = getQueryFreqs(q);

        // Write out tuned cosineSimilarity parameters
        // This is only used for grading purposes.
        // You should NOT modify the writeParaValues method.
        writeParaValues("cosinePara.txt");
        return getNetScore(tfs, q, tfQuery, d);
    }
}
