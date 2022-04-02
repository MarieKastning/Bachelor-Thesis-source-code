import java.util.*;
import java.util.stream.Collectors;

public class  DynamicCoverage {
    //colors:
    public static final String RESET = "\u001B[0m";
    public static final String CYAN = "\u001B[36m";
    public static final String RED = "\u001B[31m";


    public final String filename;
    public final MyGraph graph;
    public int k;
    public final boolean initialDRON;
    public final boolean occasionalDRON;
    public final boolean lowerBoundON;
    public final boolean upperBoundSumON;
    public final boolean upperBoundUnionON;
    public final boolean luckySolutionON;
    public final boolean luckyUpdateON;


    private HashSet<Integer> dominatingSet = new HashSet<>();
    private int lowerBound = 0;

    public int recursions = 0;
    public final int maxDegree;
    public int sizeInitialChoosables;

    /**
     * Constructor of this class.
     * @param k sets the size of the dominating set.
     */
    public DynamicCoverage(String filename, int k, boolean initialDR, boolean occasionalDR, boolean lowerBound, boolean upperBoundSum,boolean upperBoundUnion, boolean luckySolution, boolean luckyUpdate){
        this.graph = new MyGraph(filename);
        this.filename = filename;
        this.k = k;
        this.initialDRON = initialDR;
        this.occasionalDRON = occasionalDR;
        this.lowerBoundON = lowerBound;
        this.upperBoundSumON = upperBoundSum;
        this.upperBoundUnionON = upperBoundUnion;
        this.luckySolutionON = luckySolution;
        this.luckyUpdateON = luckyUpdate;
        this.maxDegree = Collections.max(graph.getVertices(), (u,v) -> graph.degree(v) - graph.degree(u));
        sizeInitialChoosables = graph.size();
    }
    /**
     * Creates a set that removes never taken nodes from the graph.
     * @return set
     */
    private LinkedList<Integer> smarterSet(LinkedList<Integer> set, int k){
        LinkedList<Integer> smarterSet = new LinkedList<>(set);
        for (Integer node : graph.getVertices()){
            if (smarterSet.size() <= k) return smarterSet;
            Integer firstNeighbor = graph.getNeighbors(node).iterator().next();
            if (graph.degree(node) == 1 && graph.degree(firstNeighbor) != 1) smarterSet.remove(node);
        }

        HashSet<Integer> useless = new HashSet<>();

        for (Integer v : smarterSet){ // computes choosables
            //compute new coverage of v
            HashSet<Integer> coverageV = getDistinctNeighbors(v);
            for (Integer u : graph.adjacencyList.get(v)){
                if (v < u || graph.degree(u) < coverageV.size() - 1 || (graph.adjacencyList.get(v).stream().mapToInt(i -> i).sum() + v) != (graph.adjacencyList.get(u).stream().mapToInt(i -> i).sum() + u)) continue;
                // compute new coverage of u
                HashSet<Integer> coverageU = getDistinctNeighbors(u);
                // compare with new coverage of v
                if ( coverageU.size() > coverageV.size() && coverageU.containsAll(coverageV)){
                    useless.add(v);
                    break;
                }
            }
        }
        smarterSet.removeAll(useless);
        smarterSet.sort((u,v)-> graph.degree(v) - graph.degree(u));
        return smarterSet;
    }
    /**
     * Counts vertices that can be reached by a set of vertices, by counting the neighbors and the vertex itself, distinctively.
     * @param vertices k-sized subset of all vertices in the graph.
     * @return amount of disctint neighbors.
     */
    public HashSet<Integer> getDistinctNeighbors (Iterable<Integer> vertices){
        HashSet<Integer> distinctNeighbors = new HashSet<>();
        for (Integer vertex : vertices){
            distinctNeighbors.add(vertex);
            distinctNeighbors.addAll(graph.adjacencyList.get(vertex));
        }
        return distinctNeighbors;
    }
    public HashSet<Integer> getDistinctNeighbors (Integer vertex){
        HashSet<Integer> distinctNeighbors = new HashSet<>();
        distinctNeighbors.add(vertex);
        distinctNeighbors.addAll(graph.getNeighbors(vertex));
        return distinctNeighbors;
    }
    public int getDistinctNeighborCount(Iterable<Integer> vertices){
        return getDistinctNeighbors(vertices).size();
    }
    public int getDistinctNeighborCount(Integer vertex){
        return getDistinctNeighbors(vertex).size();
    }

    /**
     * Recursively extracts a unique k-sized subset from the given set and adds it to the field "subsets".
     * @param subset the subset in progress.
     * @param chosables the given set - already added items.
     */
    private void buildSubsets(int indexFirst, List<Integer> subset, List<Integer> chosables, HashSet<Integer> coveredBySubset, HashMap<Integer, HashSet<Integer>> nodesNewCoverage) {
        List<Integer> iterator = new LinkedList<>(chosables);

        for (int i = indexFirst; i < iterator.size(); i++) {

            Integer element = iterator.get(i);
            if (!chosables.contains(element)) continue;
            chosables.remove(element); // remove the item to not consider it for this particular subset
            subset.add(element);

            //update coveredBySubset
            HashSet<Integer> newCoveredBySubset = new HashSet<>(coveredBySubset);
            for (Integer v : graph.getNeighbors(element)) {
                if (!newCoveredBySubset.contains(v)) newCoveredBySubset.add(v);
            }
            if (!newCoveredBySubset.contains(element)) newCoveredBySubset.add(element);

            //update nodesNewCoverage
            HashMap<Integer, HashSet<Integer>> newNodesCoverage = new HashMap<>( nodesNewCoverage.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<Integer>( e.getValue()))));
            HashSet<Integer> elementsCoverage = getDistinctNeighbors(element);
            for (Integer vertex : nodesNewCoverage.keySet()) {
                HashSet<Integer> tmp = new HashSet<>(newNodesCoverage.get(vertex));
                tmp.removeAll(elementsCoverage);
                newNodesCoverage.put(vertex,tmp);
            }

            //Termination condition
            //k is reached
            if (subset.size() == k) {
                if (newCoveredBySubset.size() > lowerBound) {
                    lowerBound = newCoveredBySubset.size();
                    dominatingSet = new HashSet<>(subset);
                    //reset
                    subset.remove(element); //remove the element again to consider other possible subsets
                    continue;
                }
            } else {
                // data reduction
                LinkedList<Integer> newChosables = new LinkedList<>(computeChoosables(subset, chosables, newCoveredBySubset, element, newNodesCoverage));
                // k cannot be reached
                if (subset.size() + newChosables.size() <= k){
                    HashSet<Integer> tmp = new HashSet<>(subset);
                    tmp.addAll(newChosables);
                    int coverage = getDistinctNeighborCount(tmp);
                    if (coverage >= lowerBound){
                        lowerBound = coverage;
                        dominatingSet = new HashSet<>(tmp);
                    }
                    //reset
                    subset.remove(element); //remove the element again to consider other possible subsets
                    continue;
                }

                //lucky solution
                if (luckySolutionON || luckyUpdateON){
                    int l = k - subset.size();
                    HashSet<Integer> union = new HashSet<>();
                    HashSet<Integer> lBest = new HashSet<>();
                    int topLsum = 0;
                    for (int j = 0; j < l;j++){
                        union.addAll(newNodesCoverage.get(newChosables.get(j)));
                        topLsum += newNodesCoverage.get(newChosables.get(j)).size();
                        lBest.add(newChosables.get(j));
                    }
                    if (topLsum == union.size() && luckySolutionON && newCoveredBySubset.size() + topLsum > lowerBound){
                        subset.addAll(lBest);
                        lowerBound = newCoveredBySubset.size() + topLsum;
                        dominatingSet = new HashSet<>(subset);
                        //reset
                        subset.remove(element); //remove the element again to consider other possible subsets
                        continue;
                    }
                    //lucky update
                    if (union.size() + newCoveredBySubset.size() > lowerBound && luckyUpdateON){
                        subset.addAll(lBest);
                        lowerBound = newCoveredBySubset.size() + union.size();
                        dominatingSet = new HashSet<>(subset);
                    }
                }

                //recursive
                if (upperBoundSumON || upperBoundUnionON){
                    int upperBound = newUB(subset, newChosables, newCoveredBySubset, newNodesCoverage);
                    if (upperBound > lowerBound) {
                        recursions++;
                        buildSubsets(0, subset, newChosables, newCoveredBySubset, newNodesCoverage);//recursive part
                    }
                }else{
                    recursions++;
                    buildSubsets(0, subset, newChosables, newCoveredBySubset, newNodesCoverage);//recursive part
                }
            }
            //reset
            subset.remove(element); //remove the element again to consider other possible subsets
        }
    }

    private LinkedList<Integer> computeChoosables(List<Integer> subset, List<Integer> original, HashSet<Integer> subsetCoverage, Integer addedNode, HashMap<Integer, HashSet<Integer>> nodesNewCoverage){
        LinkedList<Integer> chosables = new LinkedList<>(original);
        chosables.sort((u,v)-> nodesNewCoverage.get(v).size() - nodesNewCoverage.get(u).size());

        if (occasionalDRON){
            //CoverageReduction 2
            int l = k - subset.size();
            if (chosables.size() < l) return chosables;
            int acc = subsetCoverage.size();
            HashSet<Integer> removethese = new HashSet<>();
            for (int i = 0; i < l - 1; i++){
                acc += nodesNewCoverage.get(chosables.get(i)).size();
            }
            int delta = lowerBound - acc;
            //the elements that cover delta or less can never be in a result with a better coverage
            for (int i = chosables.size() - 1; i > 0; i--){
                if (nodesNewCoverage.get(chosables.get(i)).size() <= delta) removethese.add(chosables.get(i));
                else break;
            }

            chosables.removeAll(removethese);


            //Only iterate over vertices in N1 and N2 of addedNode
            HashSet<Integer> useless = new HashSet<>();
            HashSet<Integer> removable = new HashSet<>();

            for (Integer n1 : graph.adjacencyList.get(addedNode)) {
                removable.add(n1);//1.nachbarschaft
                removable.addAll(graph.adjacencyList.get(n1)); //2.nachbarschaft
            }

            for (Integer v : removable){ // computes choosables
                if (!chosables.contains(v)) continue;
                //compute new coverage of v
                HashSet<Integer> coverageV = new HashSet<>(nodesNewCoverage.get(v));
                // delete if 0
                if (coverageV.size() == 0){
                    useless.add(v);
                    continue;
                }
                for (Integer u : graph.adjacencyList.get(v)){
                    if (v < u || !chosables.contains(u) || useless.contains(u) || graph.degree(u) < coverageV.size() - 1 || (graph.adjacencyList.get(v).stream().mapToInt(i -> i).sum() + v) != (graph.adjacencyList.get(u).stream().mapToInt(i -> i).sum() + u)) continue;
                    // compute new coverage of u
                    HashSet<Integer> coverageU = new HashSet<>(nodesNewCoverage.get(u));
                    // compare with new coverage of v
                    if (coverageU.size() > coverageV.size() && coverageU.containsAll(coverageV)){
                        useless.add(v);
                        break;
                    }
                }
            }
            chosables.removeAll(useless);
        }

        return chosables;
    }

    private int newUB(List<Integer> subset, LinkedList<Integer> chosables,HashSet<Integer> subsetCoverage, HashMap<Integer, HashSet<Integer>> nodesNewCoverage){
        //sorted chosables:
        List<Integer> sortedChosables = new LinkedList<>(chosables);
        int l = k - subset.size();
        if (upperBoundSumON){
            //undominated degree UB:
            int undominatedUB = subsetCoverage.size();
            for (int i = 0; i < l; i++){
                undominatedUB += nodesNewCoverage.get(chosables.get(i)).size();
            }
            if (undominatedUB <= lowerBound || !upperBoundUnionON) return undominatedUB;
        }
        if (upperBoundUnionON){
            //new Upper Bound
            int acc = subsetCoverage.size();
            HashSet<Integer> union = new HashSet<>();
            for (int i = 0; i < l; i++){
                union.addAll(nodesNewCoverage.get(sortedChosables.get(i)));
                if (i != l-1) acc += nodesNewCoverage.get(sortedChosables.get(i)).size();
            }
            acc += nodesNewCoverage.get(sortedChosables.get(l)).size();
            return Math.max(acc, subsetCoverage.size() + union.size());
        }
        return Integer.MAX_VALUE;
    }

    public Set<Integer> getDominatingSet(){
        LinkedList<Integer> set = new LinkedList<>(graph.getVertices());
        if (initialDRON){
            set = smarterSet(set,k);
            sizeInitialChoosables = set.size();
        }
        int n = set.size();
        //Randfälle
        if (n <= k) return new HashSet<>(set);
        if (k == 0) return null;
        if (k==1){
            dominatingSet.add(Collections.max(graph.getVertices(), (w,v) -> graph.degree(w) - graph.degree(v)));
            return dominatingSet;
        }
        //initialize coverage
        HashMap<Integer, HashSet<Integer>> newCoverage = new HashMap<>();
        for (Integer u : set){
            newCoverage.put(u,getDistinctNeighbors(u));
        }
        //greedy
        int greedyLB = 0;
        if (lowerBoundON){
            greedyLB = greedyLB();
            lowerBound = greedyLB - 1;
        }
        //start recursion
        set.sort((u,v)-> graph.degree(v) - graph.degree(u));
        buildSubsets(0,new LinkedList<>(),set,new HashSet<>(), newCoverage);
        return dominatingSet;
    }

    public int greedyLB(){
        int lowerBound = 0;
        for (Integer u : graph.getVertices()){
            HashSet<Integer> subSet = new HashSet();
            subSet.add(u);
            HashSet<Integer> subsetCoverage = getDistinctNeighbors(subSet);
            HashMap<Integer, HashSet<Integer>> nodesUndominatedCoverage = new HashMap<>();
            HashSet<Integer> choosables = new HashSet<>(graph.getVertices());
            //initialize Coverage
            for (Integer v : graph.getVertices()){
                HashSet<Integer> vCoverage = getDistinctNeighbors(v);
                vCoverage.removeAll(subsetCoverage);
                nodesUndominatedCoverage.put(v,vCoverage);
            }
            //computeBestPossible
            while (subSet.size() < k){
                Integer element = Collections.max(choosables, (w,v) -> nodesUndominatedCoverage.get(w).size() - nodesUndominatedCoverage.get(v).size());
                choosables.remove(element);
                subSet.add(element);
                //update Coverage
                HashSet<Integer> elementsCoverage = getDistinctNeighbors(element);
                for (Integer vertex : nodesUndominatedCoverage.keySet()) {
                    HashSet<Integer> tmp = new HashSet<>(nodesUndominatedCoverage.get(vertex));
                    tmp.removeAll(elementsCoverage);
                    nodesUndominatedCoverage.put(vertex,tmp);
                }
                subsetCoverage.addAll(elementsCoverage);
            }
            if (subsetCoverage.size() > lowerBound){
                lowerBound = subsetCoverage.size();
            }
        }
        return lowerBound;
    }


}
