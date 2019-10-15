/**
 * 
 */
package eu.europa.ec.eurostat.eurogeostat.algo.graph;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;

import eu.europa.ec.eurostat.jgiscotools.algo.base.AffineTransformUtil;
import eu.europa.ec.eurostat.jgiscotools.datamodel.graph.Edge;
import eu.europa.ec.eurostat.jgiscotools.datamodel.graph.Face;
import eu.europa.ec.eurostat.jgiscotools.datamodel.graph.Graph;

/**
 * @author julien Gaffuri
 *
 */
public class EdgeScaling {
	private final static Logger LOGGER = Logger.getLogger(EdgeScaling.class.getName());

	//scale the edge.
	public static void scale(Edge e, double factor) { scale(e, factor, e.getGeometry().getCentroid().getCoordinate()); }
	public static void scale(Edge e, double factor, Coordinate center) {
		if(factor == 1) return;

		Graph g = e.getGraph();

		//remove edge from spatial index
		boolean b = g.removeFromSpatialIndex(e);
		if(!b) LOGGER.warn("Could not remove edge from spatial index when scaling face");

		//scale edges' internal coordinates
		for(Coordinate c : e.getCoords()){
			if(c==e.getN1().getC()) continue;
			if(c==e.getN2().getC()) continue;
			AffineTransformUtil.applyScaling(c, center, factor);
		}

		//scale nodes
		AffineTransformUtil.applyScaling(e.getN1().getC(), center, factor);
		if(!TopologyAnalysis.isClosed(e))
			AffineTransformUtil.applyScaling(e.getN2().getC(), center, factor);

		//update spatial index
		g.insertInSpatialIndex(e);

		//force face geometry update
		for(Face f : e.getFaces()) f.updateGeometry();
	}

	
	
	
}
