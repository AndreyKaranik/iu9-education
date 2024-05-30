import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputTree {

    private Node root;
    public static class Node {
        private String name;
        private List<Node> nodes;

        public Node(String name) {
            this.name = name;
            nodes = new ArrayList<>();
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public String getName() {
            return name;
        }

        public void add(Node node) {
            nodes.add(node);
        }
    }

    public OutputTree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public String toGraphviz() {
        StringBuilder sb = new StringBuilder();
        Map<Node, String> nodeIds = new HashMap<>();
        sb.append("digraph {\n");
        generateNodeIds(root, nodeIds, 1);
        for (Map.Entry<Node, String> entry : nodeIds.entrySet()) {
            sb.append("  ").append(entry.getValue())
                    .append(" [label=\"").append(entry.getKey().getName()).append("\"];\n");
        }
        generateEdges(root, nodeIds, sb);
        sb.append("}\n");
        return sb.toString();
    }

    private int generateNodeIds(Node node, Map<Node, String> nodeIds, int id) {
        String nodeId = "node" + id++;
        nodeIds.put(node, nodeId);
        for (Node child : node.getNodes()) {
            id = generateNodeIds(child, nodeIds, id);
        }
        return id;
    }

    private void generateEdges(Node node, Map<Node, String> nodeIds, StringBuilder sb) {
        for (Node child : node.getNodes()) {
            sb.append("  ").append(nodeIds.get(node)).append(" -> ").append(nodeIds.get(child)).append(";\n");
            generateEdges(child, nodeIds, sb);
        }
    }
}
