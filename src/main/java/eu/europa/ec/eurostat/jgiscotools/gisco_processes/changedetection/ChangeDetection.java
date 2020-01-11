/**
 * 
 */
package eu.europa.ec.eurostat.jgiscotools.gisco_processes.changedetection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.europa.ec.eurostat.jgiscotools.feature.Feature;
import eu.europa.ec.eurostat.jgiscotools.feature.FeatureUtil;
import eu.europa.ec.eurostat.jgiscotools.io.GeoPackageUtil;

/**
 * 
 * Analyse the differences between two versions of a datasets.
 * Compute the difference.
 * Note: both datasets are suppose to have a shared stable identifier.
 * 
 * @author julien Gaffuri
 *
 */
public class ChangeDetection<T extends Feature> {
	private final static Logger LOGGER = LogManager.getLogger(ChangeDetection.class.getName());

	//TODO make test class with fake dataset

	private Collection<T> fsIni;
	private Collection<T> fsFin;
	private String idAtt = null;

	/**
	 * @param fsIni The initial version of the dataset.
	 * @param fsFin The final version of the dataset.
	 * @param idAtt The identifier column. Set to null if the default getID() value should be used.
	 */
	public ChangeDetection(Collection<T> fsIni, Collection<T> fsFin, String idAtt) {
		this.fsIni = fsIni;
		this.fsFin = fsFin;
		this.idAtt = idAtt;
	}

	private Collection<Feature> changes = null;
	private Collection<T> unchanged = null;

	/**
	 * @return The changes.
	 */
	public Collection<Feature> getChanges() {
		if(this.changes == null) compare();
		return this.changes;
	}

	/**
	 * @return The features that have not changed.
	 */
	public Collection<T> getUnchanged() {
		if(this.unchanged == null) compare();
		return this.unchanged;
	}


	/**
	 * Compare both datasets.
	 */
	private void compare() {
		this.changes = new ArrayList<>();
		this.unchanged = new ArrayList<>();

		//list id values
		Collection<String> idsIni = FeatureUtil.getIdValues(fsIni, idAtt);
		Collection<String> idsFin = FeatureUtil.getIdValues(fsFin, idAtt);

		//index features by ids
		HashMap<String,T> indIni = FeatureUtil.index(fsIni, idAtt);
		HashMap<String,T> indFin = FeatureUtil.index(fsFin, idAtt);

		//find features present in both datasets and compare them

		//compute intersection of id sets
		Collection<String> idsInter = new ArrayList<>(idsIni);
		idsInter.retainAll(idsFin);

		for(String id : idsInter) {
			//get two corresponding features
			T fIni = indIni.get(id);
			T fFin = indFin.get(id);

			//compute change between them
			Feature d = compare(fIni, fFin, idAtt);
			
			//both versions identical. No change detected.
			if(d == null) unchanged.add(fFin);

			//change
			else changes.add(d);
		}

		//find deleted features

		//compute difference: ini - fin
		Collection<String> idsDiff = new ArrayList<>(idsIni);
		idsDiff.removeAll(idsFin);

		//retrieve deleted features
		for(String id : idsDiff) {
			T f = indIni.get(id);
			f.setAttribute("change", "D");
			changes.add(f);
		}

		//find inserted features

		//compute difference: fin - ini
		idsDiff = new ArrayList<>(idsFin);
		idsDiff.removeAll(idsIni);

		//retrieve inserted features
		for(String id : idsDiff) {
			T f = indFin.get(id);
			f.setAttribute("change", "I");
			changes.add(f);
		}

	}

	/**
	 * Compare two versions of the same feature.
	 * Both features are expected to have the same identifier and the same structure (list of attributes).
	 * The changes can be either on the attribute values, or on the geometry.
	 * 
	 * @param fIni The initial version
	 * @param fFin The final version
	 * @return A feature representing the changes.
	 */
	public static <S extends Feature> Feature compare(S fIni, S fFin, String idAtt) {
		boolean attChanged = false, geomChanged = false;
		Feature change = new Feature();

		//attributes
		int nb = 0;
		for(String att : fIni.getAttributes().keySet()) {
			Object attIni = fIni.getAttribute(att);
			Object attFin = fFin.getAttribute(att);
			if(attIni.equals(attFin)) {
				change.setAttribute(att, null);
			} else {
				attChanged = true;
				change.setAttribute(att, attFin);
				nb++;
			}
		}
		//geometry
		if( ! fIni.getDefaultGeometry().equalsTopo(fFin.getDefaultGeometry()))
			geomChanged = true;

		//no change: return null
		if(!attChanged && !geomChanged) return null;

		//set id
		String id = idAtt==null? fFin.getID() : fFin.getAttribute(idAtt).toString();
		change.setID(id);
		if(idAtt != null) change.setAttribute(idAtt, id);

		//set geometry
		change.setDefaultGeometry(fFin.getDefaultGeometry());

		//set attribute on change
		change.setAttribute("change", (geomChanged?"G":"") + (attChanged?"A"+nb:""));

		return change;
	}




	
	public Collection<Feature> getFakeChanges() {

		//copy list of changes
		ArrayList<Feature> ch_ = new ArrayList<>(getChanges());

		//build spatial index of changes

		Collection<Feature> out = new ArrayList<>();
		while(ch_.size()>0) {
			//get first element
			Feature change = ch_.get(0);
			ch_.remove(0);

			//if not inserted/removed, skip?
			//TODO
			//among changes, detect the ones that concern the same object being deleted and inserted

			//find one nearby
			//

			out .add(change);
		}		
		return out;
	}




	
	/**
	 * Return the changes from a version of a dataset to another one.
	 * 
	 * @param <T> The feature class
	 * @param fsIni The initial dataset
	 * @param fsFin The final dataset
	 * @param idAtt The identifier column. Set to null if the default getID() value should be used.
	 * @return The changes
	 */
	public static <T extends Feature> Collection<Feature> getChanges(Collection<T> fsIni, Collection<T> fsFin, String idAtt) {
		return new ChangeDetection<T>(fsIni, fsFin, idAtt).getChanges();
	}

	/**
	 * Analyse the differences between two datasets to check wether they are identical.
	 * 
	 * @param <T> The feature class
	 * @param fs1 The first dataset
	 * @param fs2 The second dataset
	 * @param idAtt The identifier column. Set to null if the default getID() value should be used.
	 * @return
	 */
	public static <T extends Feature> boolean equals(Collection<T> fs1, Collection<T> fs2, String idAtt) {
		return new ChangeDetection<T>(fs1, fs2, idAtt).getChanges().size() > 0;
	}


	public static <T extends Feature> Collection<T> applyChanges(Collection<T> fs, Collection<Feature> changes, String idAtt) {
		//TODO
		return fs;
	}





	public static void main(String[] args) {
		LOGGER.info("Start");
		String path = "src/test/resources/change_detection/";
		String outpath = "target/";

		ArrayList<Feature> fsIni = GeoPackageUtil.getFeatures(path+"ini.gpkg");
		LOGGER.info("Ini="+fsIni.size());
		ArrayList<Feature> fsFin = GeoPackageUtil.getFeatures(path+"fin.gpkg");
		LOGGER.info("Fin="+fsFin.size());

		FeatureUtil.setId(fsIni, "id");
		FeatureUtil.setId(fsFin, "id");

		//LOGGER.info("check ids:");
		//LOGGER.info( FeatureUtil.checkIdentfier(fsIni, "id") );
		//LOGGER.info( FeatureUtil.checkIdentfier(fsFin, "id") );

		ChangeDetection<Feature> cd = new ChangeDetection<>(fsIni, fsFin, "id");

		Collection<Feature> unchanged = cd.getUnchanged();
		LOGGER.info("unchanged = "+unchanged.size());
		Collection<Feature> changes = cd.getChanges();
		LOGGER.info("changes = "+changes.size());

		CoordinateReferenceSystem crs = GeoPackageUtil.getCRS(path+"ini.gpkg");
		GeoPackageUtil.save(changes, outpath+"changes.gpkg", crs, true);
		GeoPackageUtil.save(unchanged, outpath+"unchanged.gpkg", crs, true);

		LOGGER.info("End");
	}

}
