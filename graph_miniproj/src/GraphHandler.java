import ds.graph.AdjacencyMapGraph;
import ds.graph.Edge;
import ds.graph.Graph;
import ds.graph.Vertex;
import ds.positional_list.PositionalList;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GraphHandler {
    private Graph<User, Connection> g;
    public GraphHandler() {
        g = new AdjacencyMapGraph<>(false);
    }

    public Graph<User, Connection> getG() {
        return g;
    }

    public void insertUser(User us) {
        g.insertVertex(us);
    }

    public Vertex<User> getUserVert(String id) {
        for(Vertex<User> v: g.vertices()) {
            if(v.getElement().getId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    public void readFile(){
        JSONParser parser = new JSONParser();
        try {
            JSONArray a = (JSONArray) parser.parse(new FileReader("users.json"));
            for (Object o : a) {
                JSONObject person = (JSONObject) o;
                String id = (String) person.get("id");
                String name = (String) person.get("name");
                String dateOfBirth = (String) person.get("dateOfBirth");
                String universityLocation = (String) person.get("universityLocation");
                String field = (String) person.get("field");
                String workplace = (String) person.get("workplace");
                ArrayList<String> specialtiesList=new ArrayList<>();
                JSONArray specialties = (JSONArray) person.get("specialties");
                for (Object c : specialties) {
                    specialtiesList.add((String)c);
                }
                Set<String> set1=new HashSet<>();
                JSONArray connectionId = (JSONArray) person.get("connectionId");
                for (Object c : connectionId) {
                    set1.add(String.valueOf(c));
                }
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");
                User user = new User(id, name, dateOfBirth, universityLocation, field, workplace, specialtiesList, set1);
                insertUser(user);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void insertConnections() {
        for(Vertex<User> v: g.vertices()) {
            for(String i: v.getElement().getConnections()) {
                Vertex<User> u = getUserVert(i);
                if(g.getEdge(u, v) == null) {
                    g.insertEdge(u, v, new Connection(u.getElement(), v.getElement()));
                }
            }
        }
    }
    public ArrayList<User> getSuggestion(String id) {
        Vertex<User> user = getUserVert(id);
        Map<Map<Vertex<User>, Edge<Connection>>, Integer> m = ((AdjacencyMapGraph<User, Connection>)g).BFS(user);
        ArrayList<User> users = new ArrayList<>();
        for(Map.Entry<Map<Vertex<User>, Edge<Connection>>, Integer> e: m.entrySet()) {
            for(Map.Entry<Vertex<User>, Edge<Connection>> e1: e.getKey().entrySet()) {
                e1.getKey().getElement().setTarget(new Target(user.getElement(), e.getValue()));
                if(!e.getValue().equals(1)) {
                    users.add(e1.getKey().getElement());
                }

                //System.out.println(e.getValue() + " " + e1.getKey().getElement().getId());
            }
        }
        Collections.sort(users);
        return users;
    }

    public ArrayList<Vertex<User>> getInfluenceUsers (){
        Centrality centrality = new Centrality((AdjacencyMapGraph<User, Connection>)g);
        ArrayList<ArrayList<Node>> katz = centrality.katzCentrality(0.5);
        ArrayList<ArrayList<Node>> betweenness = centrality.betweennessCentrality();
        ArrayList<ArrayList<Node>> closeness = centrality.closenessCentrality();

        PositionalList<Vertex<User>> allUsers = ((AdjacencyMapGraph<User , Connection > ) g ).getVertices();
        ArrayList<Map.Entry<Vertex<User>, Double>> scoresList = new ArrayList<>();

        for (Vertex<User> influence : allUsers) {
            double score = 0.0;
            score += (getKatzCentralityScore((influence.getElement()).getId(), katz)/1.5);
            score += (getBetweennessCentralityScore((influence.getElement()).getId(), betweenness)/500);
            score += (getClosenessCentralityScore((influence.getElement()).getId(), closeness)*500);
            scoresList.add(new AbstractMap.SimpleEntry<>(influence, score));
        }

        scoresList.sort(new Comparator<Map.Entry<Vertex<User>, Double>>() {
            @Override
            public int compare(Map.Entry<Vertex<User>, Double> o1, Map.Entry<Vertex<User>, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        ArrayList<Vertex<User>> influenceUsers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            influenceUsers.add(scoresList.get(i).getKey());
        }

        return influenceUsers;
    }

    private double getKatzCentralityScore(String id, ArrayList<ArrayList<Node>> katz) {
        for (ArrayList<Node> nodes : katz) {
            for (Node node : nodes) {
                if (node.getUser().getId().equals(id)) {
                    return node.getValue();
                }
            }
        }
        return 0;
    }

    private double getBetweennessCentralityScore(String id, ArrayList<ArrayList<Node>> betweenness) {
        for (ArrayList<Node> nodes : betweenness) {
            for (Node node : nodes) {
                if (node.getUser().getId().equals(id)) {
                    return node.getValue();
                }
            }
        }
        return 0;
    }

    private double getClosenessCentralityScore(String id, ArrayList<ArrayList<Node>> closeness) {
        for (ArrayList<Node> nodes : closeness) {
            for (Node node : nodes) {
                if (node.getUser().getId().equals(id)) {
                    return node.getValue();
                }
            }
        }
        return 0;
    }
}
