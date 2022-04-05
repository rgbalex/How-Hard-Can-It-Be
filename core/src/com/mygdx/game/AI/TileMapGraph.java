package com.mygdx.game.AI;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.utils.QueueFIFO;

import static com.mygdx.utils.TileMapCells.OBSTACLE;
import static com.mygdx.utils.TileMapCells.PASSABLE;

/**
 * The Graphical representation of the tilemap with each cell being bidirectionally to the adjacent nodes.
 */
public class TileMapGraph implements IndexedGraph<Node> {
    private final NodeHeuristic heuristic;
    private final Array<Node> nodes;
    private final Array<Path> paths;
    private final Vector2 mapDim;

    private final ObjectMap<Node, Array<Connection<Node>>> nodePaths;

    private TileMapGraph() {

        heuristic = new NodeHeuristic();
        nodes = new Array<>();
        paths = new Array<>();
        nodePaths = new ObjectMap<>();
        mapDim = new Vector2();
    }


    /**
     * Creates a Graph from the given tilemap
     *
     * @param map the source tilemap
     */
    public TileMapGraph(TiledMap map) {
        this();

        MapLayers layers = map.getLayers();
        TiledMapTileLayer layer = null;

        // find the collision layer
        for (MapLayer l : layers) {
            if (l.getName().equals("Collision")) {
                layer = (TiledMapTileLayer) l;
            }
        }
        // if there is no collision layer
        if (layer == null) {
            return;
        }
        // the map dimensions
        mapDim.set(layer.getWidth(), layer.getHeight());

        nodes.ensureCapacity((int) mapDim.x * (int) mapDim.y);

        // create all the nodes
        for (int i = 0; i < mapDim.x * mapDim.y; i++) {
            nodes.add(new Node(0, 0));
        }

        // for each column
        for (int x = 0; x < layer.getWidth(); x++) {
            // for each row
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell center = layer.getCell(x, y);

                if (getType(center) == OBSTACLE) {
                    continue;
                }
                // the central node
                addNode(x, y);

                // all surrounding nodes
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {

                        // prevents the node pathing to its self
                        if (i == 0 && j == 0) {
                            continue;
                        }

                        TiledMapTileLayer.Cell cell = layer.getCell(x + i, y + j);
                        // is cell outside the map
                        if (cell == null) {
                            continue;
                        }

                        // is the cell passable
                        if (cell.getTile().getId() == PASSABLE) {

                            addNode(x + i, y + j);
                            addPath(x, y, x + i, y + j);
                        }
                    }
                }
            }
        }
    }

    /**
     * Node a position (x, y)
     *
     * @param x co-ord
     * @param y co-ord
     * @return Node at (x, y) or null
     */
    public Node getNode(float x, float y) {
        Node n = nodes.get(getIndex(x, y));
        if (n.cost == -1) {
            return null;
        }
        return n;
    }

    private int getIndex(float x, float y) {
        return (int) (mapDim.x * y + x);
    }

    /**
     * @return type of cell as far as the tile map is condensed
     */
    private int getType(TiledMapTileLayer.Cell c) {
        return c.getTile().getId();
    }

    private int getIndex(int x, int y) {
        return (int) mapDim.x * y + x;
    }

    /**
     * doesn't add if already there
     *
     * @param x x pos
     * @param y y pos
     */
    private void addNode(float x, float y) {
        Node n = nodes.get(getIndex((int) x, (int) y));
        if (n.cost > 0) {
            return;
        }
        n.set(x, y);
        n.cost = 1;
    }

    /**
     * Adds path to map doesn't check for duplicates
     *
     * @param a src
     * @param b dst
     */
    private void addPath(Node a, Node b) {
        Path path = new Path(a, b);

        if (!nodePaths.containsKey(a)) {
            nodePaths.put(a, new Array<>());
        }
        nodePaths.get(a).add(path);
        paths.add(path);
    }

    /**
     * Adds path to map doesn't check for duplicates
     *
     * @param x1 src.x
     * @param y1 src.y
     * @param x2 dst.x
     * @param y2 dst.y
     */
    private void addPath(float x1, float y1, float x2, float y2) {
        Node a = getNode(x1, y1);
        Node b = getNode(x2, y2);

        addPath(a, b);
    }

    /**
     * Finds the path
     *
     * @param start the starting node
     * @param goal  the ending node
     * @return a queue of the nodes to visit null if no path id found
     */
    public GraphPath<Node> findPath(Node start, Node goal) {
        if (start == null || goal == null) {
            return null;
        }
        GraphPath<Node> path = new DefaultGraphPath<>();
        new IndexedAStarPathFinder<>(this).searchNodePath(start, goal, heuristic, path);
        return path;
    }

    /**
     * Finds a sequence of locations which can be travelled to without collision
     *
     * @param a starting node
     * @param b destination node
     * @return queue of location to travel to in order
     */
    public QueueFIFO<Vector2> findOptimisedPath(Node a, Node b) {
        GraphPath<Node> path = findPath(a, b);
        QueueFIFO<Vector2> res = new QueueFIFO<>();
        Vector2 delta = new Vector2();
        float sequenceLength = 0; // the amount of times a
        Vector2 cur = new Vector2();

        Vector2 prev = path.get(0).getPosition();
        for (int i = 1; i < path.getCount(); i++) {
            Node n = path.get(i);
            cur.set(n.getPosition());
            // d contains the current vector between the current pos an prev
            Vector2 d = cur.cpy();
            d.sub(prev);


            if (delta.x == d.x && delta.y == d.y) {
                sequenceLength++;
            } else {
                res.add(delta.scl(sequenceLength));
                delta = d;
                sequenceLength = 1;
            }
            prev.set(cur);
        }
        res.remove(0);
        res.add(delta.scl(sequenceLength));
        return res;
    }

    /**
     * Finds a sequence on locations which can be travelled to without collision
     *
     * @param a starting node location
     * @param b destination node location
     * @return queue of location to travel to in order
     */
    public QueueFIFO<Vector2> findOptimisedPath(Vector2 a, Vector2 b) {
        Node n1 = getNode(a.x, a.y);
        Node n2 = getNode(b.x, b.y);
        return findOptimisedPath(n1, n2);
    }

    /**
     * Finds a sequence on locations which can be travelled to without collision
     *
     * @param x1 starting node x co-ords tile space
     * @param y1 starting node y co-ords tile space
     * @param x2 destination node x co-ords tile space
     * @param y2 destination node y co-ords tile space
     * @return queue of location to travel to in order
     */
    public QueueFIFO<Vector2> findOptimisedPath(float x1, float y1, float x2, float y2) {
        Node a = getNode(x1, y1);
        Node b = getNode(x2, y2);
        return findOptimisedPath(a, b);
    }


    // The Interface
    @Override
    public int getIndex(Node node) {
        return getIndex(node.getPosition().x, node.getPosition().y);
    }

    @Override
    public int getNodeCount() {
        return (int) (mapDim.x * mapDim.y);
    }

    @Override
    public Array<Connection<Node>> getConnections(Node fromNode) {
        if (nodePaths.containsKey(fromNode)) {
            return nodePaths.get(fromNode);
        }

        return new Array<>();
    }

    public Array<Node> getNodes(){return nodes;}
}
