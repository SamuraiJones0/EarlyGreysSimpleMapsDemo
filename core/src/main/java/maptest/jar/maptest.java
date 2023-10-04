package maptest.jar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import space.earlygrey.simplegraphs.Edge;
import space.earlygrey.simplegraphs.Path;
import space.earlygrey.simplegraphs.UndirectedGraph;
import space.earlygrey.simplegraphs.utils.Heuristic;

import java.util.Collection;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class maptest extends ApplicationAdapter {
    int MapWidth;
    int MapHeight;
    SimpleCell[][] Map;

    UndirectedGraph<SimpleCell> Graph;
    public static final TheHeuristicClass TheHeuristic = new TheHeuristicClass();
    /////////////////////////////////////////////////////
    // SimpleCell will be the class for our Vertices.
    /////////////////////////////////////////////////////
    public class SimpleCell {
        int X;
        int Y;
        boolean Blocked;
        public SimpleCell(int x, int y, boolean blocked) {
            X = x;
            Y = y;
            Blocked = blocked;
        }
    }

    @Override
    public void create() {
        //This is intended to function as a black box -- all its info
        //is written to the globals, so it should not be necessary to
        //dig into the details.
        ImportMapFromTiled();
        //After ImportMapFromTiled(), MapWidth, MapHeight and Map will be populated.

        //init the graph
        Graph = new UndirectedGraph<>();
        //add the vertices
        for( int x = 0; x < MapWidth; x++){
            for( int y = 0; y < MapWidth; y++) {
                Graph.addVertex(Map[x][y]);
            }
        }

        //add the edges
        for( int x = 0; x < MapWidth; x++){
            for( int y = 0; y < MapWidth; y++) {
                //we're using an undirected graph, so we're only considering connections
                //to the spaces N, NE, E, SE.
                // S, SW, W, NW will be the reverse direction of edges from other Cells.
                if(y + 1 < MapHeight)
                    AddAppropriateEdges(Map[x][y], Map[x][y+1]);
                if(x + 1 <MapWidth && y + 1 < MapHeight)
                    AddAppropriateEdges(Map[x][y], Map[x+1][y+1]);
                if(x + 1 < MapWidth)
                    AddAppropriateEdges(Map[x][y], Map[x+1][y]);
                if(x + 1 < MapWidth && y-1 >= 0)
                    AddAppropriateEdges(Map[x][y], Map[x+1][y-1]);
            }
        }


    }

    @Override
    public void render() {
        /////////////////////////////////////////////////////
        // Get paths
        /////////////////////////////////////////////////////
        Path<SimpleCell> path1 = Graph.algorithms().findShortestPath(Map[79][76],Map[50][90], TheHeuristic);

        //path 1 includes a bit where it goes from Map[82][72] to Map[82][71]
        //let's assume that path is more expensive and try again.

        Collection<Edge<SimpleCell>> edges = Graph.getEdges(Map[79][76]);
        for(Edge<SimpleCell> edge : edges) {
            edge.setWeight(100000000);
        }

        Path<SimpleCell> path2 = Graph.algorithms().findShortestPath(Map[79][76],Map[50][90], TheHeuristic);

        /////////////////////////////////////////////////////
        // Write results to window and exit.
        /////////////////////////////////////////////////////
        int totalSteps = Math.min(path1.size(), path2.size());
        System.out.println("Path 1:                   Path 2:");
        for (int i = 0; i<totalSteps; i++){
            System.out.println(String.format(
                "Node %d: X: %2d, Y: %3d     X: %4d, Y: %5d",
                i, path1.get(i).X, path1.get(i).Y, path2.get(i).X, path2.get(i).Y));
        }
        Gdx.app.exit();
    }



    public void AddAppropriateEdges(SimpleCell from, SimpleCell to) {
        //we're using an undirected graph, so we're only considering connections
        //to the spaces N, NE, E, SE.
        // S, SW, W, NW will be the reverse direction of edges from other Cells.
        if(!from.Blocked && !to.Blocked){
            float distance = (float) Math.sqrt((from.X - to.X) * (from.X - to.X) + (from.Y - to.Y) * (from.Y - to.Y));
            Graph.addEdge(from,to,distance);
        }
    }

    @Override
    public void dispose() {

    }

    /////////////////////////////////////////////////////
    // Heuristic
    /////////////////////////////////////////////////////
    private static class TheHeuristicClass implements Heuristic<SimpleCell> {
        @Override
        public float getEstimate(SimpleCell from, SimpleCell to) {
            return (float) Math.sqrt((from.X - to.X) * (from.X - to.X) + (from.Y - to.Y) * (from.Y - to.Y));
        }
    }

    ////////////////////////////////////////
    // Scary Tiled stuff below here.
    ////////////////////////////////////////
    public void ImportMapFromTiled() {
        TiledMap _map = new TmxMapLoader().load("tiled/realmap1.tmx");
        MapWidth = _map.getProperties().get("width", Integer.class);
        MapHeight = _map.getProperties().get("height", Integer.class);

        Map = new SimpleCell[MapWidth][MapHeight];
        for( int x = 0; x < MapWidth; x++){
            for( int y = 0; y < MapWidth; y++){
                Map[x][y] = new SimpleCell(x,y,CheckTileBlockage(x,y,_map));
            }
        }
    }

    public boolean CheckTileBlockage(int x, int y, TiledMap map) {
        boolean blockage = false;
        for (MapLayer refLayer : map.getLayers()) {
            if (refLayer.getClass() == TiledMapTileLayer.class) {
                TiledMapTileLayer layer = (TiledMapTileLayer) refLayer;
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    TiledMapTile tile = layer.getCell(x, y).getTile();
                    if (tile.getObjects().getCount() != 0) {
                        blockage = true;
                    }
                }
            }
        }
        return blockage;
    }
}
