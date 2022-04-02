
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

/**
 * implements the interface Graph
 * @author Marie Kastning
 */
public class MyGraph implements Graph {

    public Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
    private int edgeCounter = 0; //saves the amount of edges in the graph
    private HashMap<Integer,String> vertexNames;

    /**
     * A constructor of this class.
     * Using this constructor you will create an empty graph and will be able to build it using the given class-methods.
     */
    public MyGraph(){}
    /**
     * A constructor of this class.
     * Using this constructor you will import a file by passing the filename.
     * The file should contain Edges.
     * The pattern is: Each line represents one edge; an edge is presented by 2 vertices divided by a blank space.
     */
    public MyGraph (String filename) {

        HashMap<String, Integer> seenIds = new HashMap<String, Integer>();
        vertexNames = new HashMap<Integer, String>();
        File file = new File(filename);

        try {
            final LineNumberReader reader = new LineNumberReader(new FileReader(file));
            String str;
            int id = 0;
            while ((str = reader.readLine()) != null)
            {
                str = str.trim();
                if (!str.startsWith("#") && !str.startsWith("%") ) {
                    StringTokenizer tokens = new StringTokenizer(str);
                    if (tokens != null && tokens.countTokens() > 1) {
                        String vertexA = tokens.nextToken();
                        String vertexB = tokens.nextToken();
                        if (!seenIds.containsKey(vertexA)) {
                            seenIds.put(vertexA, id);
                            addVertex(id,vertexA);
                            id++;
                        }
                        if (!seenIds.containsKey(vertexB)) {
                            seenIds.put(vertexB, id);
                            addVertex(id,vertexB);
                            id++;
                        }
                        addEdge(seenIds.get(vertexA), seenIds.get(vertexB));
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Could not locate input file '"+filename+"'.");
            System.exit(0);
        }
    }

    @Override
    public void addVertex(Integer v) {
        addVertex(v,v.toString());
    }
    public void addVertex (Integer v, String name) {
        if (contains(v)){
            System.out.println("Vertex with chosen designation already exists");
        }else{
            vertexNames.put(v,name);
            adjacencyList.put(v, new ArrayList<>());
        }
    }

    @Override
    public void addEdge(Integer v, Integer w) {
        if (!contains(v)){
            addVertex(v);
        }if(!contains(w)){
            addVertex(w);
        }if (!adjacent(v,w)){
            adjacencyList.get(v).add(w);
            adjacencyList.get(w).add(v);
            edgeCounter++;
        }
    }

    @Override
    public void deleteVertex(Integer v) {
        if (contains(v)){
            for (Integer x : adjacencyList.get(v)) {
                adjacencyList.get(x).remove(v);
                edgeCounter--;
            }
            adjacencyList.remove(v);
        }
    }

    @Override
    public void deleteEdge(Integer u, Integer v) {
        if (!(contains(u) && contains(v))){
            System.out.println("At least on of the Vertices doesn't exist.");
        }else{
            if (adjacent(v,u)){
                adjacencyList.get(v).remove(u);
                adjacencyList.get(u).remove(v);
                edgeCounter--;
            }
        }
    }

    @Override
    public boolean contains(Integer v) {
        return adjacencyList.containsKey(v);
    }

    @Override
    public int degree(Integer v) {
        return adjacencyList.get(v).size();
    }

    @Override
    public boolean adjacent(Integer v, Integer w) {
        return adjacencyList.get(v).contains(w);
    }

    @Override
    public Graph getCopy() {
        MyGraph copy = new MyGraph();
        Map <Integer,List<Integer>> copyAdjlist = new HashMap<>();
        for (Integer x : getVertices()) {
            copy.addVertex(Integer.valueOf(x));
        }
        for (Integer x : getVertices()) {
            for (Integer y : getNeighbors(Integer.valueOf(x))) {
                copy.addEdge(y,x);
            }
        }
        return copy;
    }

    @Override
    public Set<Integer> getNeighbors(Integer v) {
        return new HashSet<>(adjacencyList.get(v));
    }

    @Override
    public int size() {
        return adjacencyList.size();
    }

    @Override
    public int getEdgeCount() {
        return edgeCounter;
    }

    @Override
    public Set<Integer> getVertices() {
        return adjacencyList.keySet();
    }


}
