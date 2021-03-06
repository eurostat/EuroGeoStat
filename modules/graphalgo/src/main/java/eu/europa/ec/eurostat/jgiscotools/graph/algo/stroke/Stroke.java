/**
 * 
 */
package eu.europa.ec.eurostat.jgiscotools.graph.algo.stroke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import eu.europa.ec.eurostat.jgiscotools.algo.base.Union;
import eu.europa.ec.eurostat.jgiscotools.feature.Feature;
import eu.europa.ec.eurostat.jgiscotools.graph.base.structure.Edge;

/**
 * @author julien Gaffuri
 *
 */
public class Stroke extends Feature {
	private List<Feature> sections = new ArrayList<>();
	public List<Feature> getSections() { return sections; }

	public Stroke(Collection<Edge> edges) {

		//set list of features
		for(Edge e : edges) sections.add( (Feature)e.obj );

		//build and set geometry
		Collection<Geometry> gs = new ArrayList<Geometry>();
		for(Edge e : edges) gs.add(e.getGeometry());
		this.setGeometry( Union.getUnionAsLineString(gs) );

		//set initial value for salience
		setAttribute("sal",-1);
	}

}
