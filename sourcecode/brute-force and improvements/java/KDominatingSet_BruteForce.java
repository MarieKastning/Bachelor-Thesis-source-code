import java.util.*;

/**
 * Defines methods to compute the k-sized dominating set in a graph.
 * Brute-Force algorithm.
 * @author Marie Kastning
 */
public class KDominatingSet_BruteForce {

    private final MyGraph graph;
    private final int k;

    /**
     * Constructor of this class.
     * @param graph an undirected graph.
     * @param k sets the size of the dominating set.
     */
    public KDominatingSet_BruteForce(MyGraph graph, int k){
        this.graph = graph;
        this.k = k;
    }

    /**
     * Counts vertices that can be reached by a set of vertices, by counting the neighbors and the vertex itself, distinctively.
     * @param vertices k-sized subset of all vertices in the graph.
     * @return amount of disctint neighbors.
     */
    private int getDistinctNeighborCount (Set<Integer> vertices){
        HashSet<Integer> distinctNeighbors = new HashSet<>();
        if (vertices == null) return distinctNeighbors;
        for (Integer vertex : vertices){
            distinctNeighbors.add(vertex);
            distinctNeighbors.addAll(graph.getNeighbors(vertex));
        }
        return distinctNeighbors.size();
    }

    /**
     * Determines the dominating set by choosing the set with the biggest "getNeighborCount".
     * @param sets all possible k-sized subsets of all vertices in the graph.
     * @return dominating set of size k.
     */
    private Set<Integer> getDominatingSet(Set<Set<Integer>> sets){
        Set<Integer> max = sets.iterator().next();
        int nodes = getNeighborCount(max);
        for (Set<Integer> set : sets){
            if (getNeighborCount(set) > nodes){
                max = set;
                nodes = getNeighborCount(set);
            }
        }
        return max;
    }

    /**
     * Calls "getAllSubsetsOfK" to pass on the computed subsets to "getDominatingSet(subsets)".
     * @return the Dominating set in graph of size k.
     */
    public Set<Integer> getDominatingSet(){
        return getDominatingSet(SubSets.getAllSubsetsOfK(k,graph.getVertices()););
    }

    /**
     * Implements Methods to extract the possible k-sized subsets in a given set.
     */
    private static class SubSets{
        private static Set<Set<Integer>> subsets = new HashSet<>();

        /**
         * Manages edge cases and calls "buildSubsets" to extract the k-sized subsets in a given set.
         * Calls MathPlus.AmountKSizedSubSetsinSet to check whether the amount of extracted subsets is as expected.
         * @param k amount of vertices per subset.
         * @param set the given set.
         * @return extracted subsets.
         */
        private static Set<Set<Integer>>getAllSubsetsOfK(int k, Set<Integer> set){
            int n = set.size();
            if (n < k) throw new IllegalArgumentException();
            if (n == k) {
                subsets.add(set);
            }else{
                List<Integer> original = new LinkedList<>(set);
                buildSubsets(k,0,new LinkedList<>(),original);
            }
            return subsets;
        }

        /**
         * Recursively extracts a unique k-sized subset from the given set and adds it to the field "subsets".
         * @param k amount of vertices per subset.
         * @param indexFirst manages the items that are considered in the given set.
         * @param subset the subset in progress.
         * @param original the given set - already added items.
         */
        private static void buildSubsets(int k, int indexFirst, List<Integer> subset, List<Integer> original){
            if (subset.size() == k){
                subsets.add(new HashSet<>(subset));
            }else{
                for (int i = indexFirst; i < original.size(); i++){
                    Integer element = original.get(i);
                    original.remove(element); // remove the item to not consider it for this particular subset
                    subset.add(element);
                    buildSubsets(k,i,subset,original); //recursive part
                    original.add(i,element); //add it to consider it in other possible subsets
                    subset.remove(element); //remove the element again to consider other possible subsets
                }
            }
        }
    }
}
