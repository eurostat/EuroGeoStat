package eu.ec.estat.geostat.nuts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import eu.ec.estat.java4eurostat.base.StatsHypercube;
import eu.ec.estat.java4eurostat.base.StatsIndex;
import eu.ec.estat.java4eurostat.io.EurobaseIO;
import eu.ec.estat.java4eurostat.io.EurostatTSV;

/**
 * 
 * @author julien Gaffuri
 *
 */
public class NUTSUtils {

	public static void main(String[] args) {
		//for(int time=1985; time<=2020; time++) System.out.println(getNUTSPopulation("FR", time));
		for(int time=1985; time<=2020; time++) {
			System.out.println(getNUTSPopulation("FR", time) / getNUTSArea("FR", time));
		}
		//EurostatTSV.load("stat_cache/demo_r_d3area.tsv").selectDimValueEqualTo("unit","KM2","geo","FR").printInfo();
	}


	//compute figures divided by nuts area
	public StatsHypercube computeDensityFigures(StatsHypercube sh){
		StatsHypercube out = null;
		//TODO
		return out;
	}

	//compute figures divided by nuts population
	public StatsHypercube computePopRatioFigures(StatsHypercube sh){ return computePopRatioFigures(sh, 1000); }
	public StatsHypercube computePopRatioFigures(StatsHypercube sh, int multi){
		StatsHypercube out = null;
		//TODO
		return out;
	}



	//Population on 1 January by broad age group, sex and NUTS 3 region (demo_r_pjanaggr3)	AGE=TOTAL;SEX=T;UNIT="NR"
	private static StatsIndex nutsPop = null;
	public static double getNUTSPopulation(String nutsCode, int time){
		if(nutsPop == null){
			EurobaseIO.update("stat_cache/", "demo_r_pjanaggr3");
			nutsPop = new StatsIndex(
					EurostatTSV.load("stat_cache/demo_r_pjanaggr3.tsv").selectDimValueEqualTo("age","TOTAL", "sex","T", "unit","NR")
					.delete("age").delete("sex").delete("unit"),
					"time", "geo"
					);
		}
		return nutsPop.getSingleValue(time+" ", nutsCode);
	}

	//Area by NUTS 3 region (demo_r_d3area) LANDUSE=L0008;TOTAL  UNIT=KM2
	private static StatsIndex nutsArea = null;
	public static double getNUTSArea(String nutsCode, int time){ return getNUTSArea(nutsCode, time, "TOTAL"); }
	public static double getNUTSArea(String nutsCode, int time, String landuse){
		if(nutsArea == null){
			EurobaseIO.update("stat_cache/", "demo_r_d3area");
			nutsArea = new StatsIndex(
					EurostatTSV.load("stat_cache/demo_r_d3area.tsv").selectDimValueEqualTo("unit","KM2")
					.delete("unit"),
					"landuse", "time", "geo"
					);
		}
		return nutsArea.getSingleValue(landuse, time+" ", nutsCode);
	}





	private static class NUTSChange{
		public String codeIni, codeFin, change, explanation;
		@Override
		public String toString() { return codeIni+" -> "+codeFin+" - "+change+" - "+explanation; }
	}

	private static HashSet<NUTSChange> changes2010_2013 = null;
	private static HashMap<String,NUTSChange> changes2010_2013I1 = null;
	private static void load2010_2013(){
		if(changes2010_2013 != null) return;
		changes2010_2013 = new HashSet<NUTSChange>();
		changes2010_2013I1 = new HashMap<String,NUTSChange>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("resources/nuts_changes/NUTS_changes_2010_2013.csv"));
			//skip first line
			String line = br.readLine();
			//read data
			while ((line = br.readLine()) != null) {
				String[] elts = line.split(",", -1);
				if(elts.length != 4) System.out.println(elts.length);
				NUTSChange nc = new NUTSChange(); nc.codeIni=elts[0]; nc.codeFin=elts[1]; nc.change=elts[2]; nc.explanation=elts[3];
				changes2010_2013.add(nc);
				if(nc.codeIni != null && !"".equals(nc.codeIni)) changes2010_2013I1.put(nc.codeIni, nc);
			}
		} catch (IOException e) { e.printStackTrace();
		} finally {
			try { if (br != null)br.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
	}

	public String get2010To2013Code(String code2010){
		load2010_2013();
		NUTSChange nc = changes2010_2013I1.get(code2010);
		if(nc == null) return null;
		if("".equals(nc.codeFin)) return null;
		return nc.codeFin;
	}

}


//Boundary shift
//Code change
//Code; name change
//Merged
//Name change
//New region
//Split

