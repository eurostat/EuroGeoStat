package eu.europa.ec.eurostat.jgiscotools.algo.polygon;

import java.util.HashSet;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.valid.IsValidOp;

import eu.europa.ec.eurostat.jgiscotools.algo.deformationgael.CSegmentLength;
import eu.europa.ec.eurostat.jgiscotools.algo.deformationgael.CSegmentOrientation;
import eu.europa.ec.eurostat.jgiscotools.algo.deformationgael.GAELDecomposers;
import eu.europa.ec.eurostat.jgiscotools.algo.deformationgael.GAELDeformable;
import eu.europa.ec.eurostat.jgiscotools.algo.deformationgael.GAELPoint;
import eu.europa.ec.eurostat.jgiscotools.algo.deformationgael.SMSegment;
import eu.europa.ec.eurostat.jgiscotools.algo.measure.Orientation;


public class Squarring {

	public static Polygon get(Polygon poly) {
		return get(poly, 20, -1, 0.01);
	}

	public static Polygon get(Polygon poly, double angleToleranceDeg, int pointActivationLimitNb, double res) {
		Polygon poly2 = (Polygon) poly.copy();
		HashSet<GAELPoint> ps = new HashSet<GAELPoint>();
		HashSet<SMSegment> segs = new HashSet<SMSegment>();
		GAELDecomposers.decomposeLimit(poly2, res, ps, segs, false, null);

		double o=new Orientation(poly2).getSidesOrientation();

		for (SMSegment s : segs) {

			//length preservation
			new CSegmentLength(s, 1.0);

			//segment orientation (modulo Pi/2)
			double so=s.getOrientation();
			double segOrientationModuloHalfPi = 0.0;
			if (so < -Math.PI/2) segOrientationModuloHalfPi = so+Math.PI;
			else if ( so >= -Math.PI/2 && so < 0 ) segOrientationModuloHalfPi = so+Math.PI/2;
			else if ( so >= 0 && so < Math.PI/2 ) segOrientationModuloHalfPi = so;
			else if ( so > Math.PI/2 ) segOrientationModuloHalfPi = so-Math.PI/2;
			if ( so == Math.PI ) segOrientationModuloHalfPi = 0;

			if (Math.abs(segOrientationModuloHalfPi-o)*180/Math.PI <= angleToleranceDeg) {
				//orientation close from the polygon sides orientation
				double so2 = 0.0;
				if (so < -Math.PI/2) so2=o-Math.PI;
				else if ( so >= -Math.PI/2 && so < 0 ) so2 = o-Math.PI/2;
				else if ( so >= 0 && so < Math.PI/2 ) so2 = o;
				else if ( so > Math.PI/2 ) so2 = o+Math.PI/2;
				if ( so == Math.PI ) so2 = 0.0;
				//orientation change
				new CSegmentOrientation(s, 10.0, so2);
			}
			else {
				//orientation preservation
				new CSegmentOrientation(s, 2.0);
			}
		}

		GAELDeformable def = new GAELDeformable(ps);
		def.activatePoints(pointActivationLimitNb * ps.size(), res);

		if (!IsValidOp.isValid(poly2)) return poly;
		else if (poly2.isEmpty()) return poly;
		else if (poly2.getArea() < 0.0001) return poly;

		def.clean();
		return poly2;
	}
}
