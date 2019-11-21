/**
 * 
 */
package eu.europa.ec.eurostat.jgiscotools.gisco_processes;

import java.util.Collection;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.europa.ec.eurostat.jgiscotools.feature.Feature;
import eu.europa.ec.eurostat.jgiscotools.io.GeoPackageUtil;
import eu.europa.ec.eurostat.jgiscotools.io.SHPUtil;

/**
 * @author gaffuju
 *
 */
public class TestGDBRead {

	public static void main(String[] args) throws Exception {
		//See https://docs.geotools.org/stable/userguide/library/data/ogr.html
		//See https://gis.stackexchange.com/questions/243746/using-geotools-to-open-esri-file-geodatabase-filegdb
		
		String egpath = "E:/dissemination/shared-data/";

		System.out.println("load");
		Collection<Feature> networkSections = SHPUtil.loadSHP(egpath+"ERM/ERM_2019.1_shp_LAEA/Data/RoadL_RTT_14_15_16.shp").fs;
		System.out.println("load");
		networkSections.addAll( SHPUtil.loadSHP(egpath+"ERM/ERM_2019.1_shp_LAEA/Data/RoadL_RTT_984.shp").fs );
		System.out.println("load");
		networkSections.addAll( SHPUtil.loadSHP(egpath+"ERM/ERM_2019.1_shp_LAEA/Data/RoadL_RTT_0.shp").fs );

		System.out.println("save");
		CoordinateReferenceSystem crs = CRS.decode("EPSG:3035");
		GeoPackageUtil.save(networkSections, "E:/workspace/gridstat/data/RoadL.gpkg", crs, true);

		System.out.println("end");
	}

}
