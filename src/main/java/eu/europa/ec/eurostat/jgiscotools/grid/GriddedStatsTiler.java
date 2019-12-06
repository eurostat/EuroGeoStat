/**
 * 
 */
package eu.europa.ec.eurostat.jgiscotools.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import eu.europa.ec.eurostat.java4eurostat.base.Stat;
import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.io.CSV;
import eu.europa.ec.eurostat.java4eurostat.util.StatsUtil;

/**
 * 
 * Utility to create tiles of gridded statistics.
 * 
 * @author Julien Gaffuri
 *
 */
public class GriddedStatsTiler {

	/**
	 * The statistical figures to tile
	 */
	private StatsHypercube sh;

	/**
	 * The name of the attribute with the grid id
	 */
	private String gridIdAtt = "GRD_ID";

	/**
	 * The position of origin of the grid to take into account to defining the tiling frame.
	 * It should be the bottom left corner of the tiling frame.
	 * Tiling numbering goes from left to right, and from bottom to top.
	 * For LAEA, take (0,0).
	 */
	private Coordinate originPoint = new Coordinate(0,0);


	/**
	 * The tile resolution, in number of grid cells.
	 */
	private int tileResolutionPix = 256;

	/**
	 * The computed tiles.
	 */
	private Collection<GridStatTile> tiles;
	public Collection<GridStatTile> getTiles() { return tiles; }

	private class GridStatTile {
		public int x,y;
		public ArrayList<Stat> stats = new ArrayList<Stat>();
		GridStatTile(int x, int y) { this.x=x; this.y=y; }
		public Stat getMaxValue() {
			Stat s_ = null;
			for(Stat s : stats) if (s_==null || s.value > s_.value) s_=s;
			return s_;
		}
		public Stat getMinValue() {
			Stat s_ = null;
			for(Stat s : stats) if (s_==null || s.value < s_.value) s_=s;
			return s_;
		}
	}

	public GriddedStatsTiler(String csvFilePath, String statAttr, int tileResolutionPix) {
		this( CSV.load(csvFilePath, statAttr), tileResolutionPix );
	}

	public GriddedStatsTiler(StatsHypercube sh, int tileResolutionPix) {
		this.sh = sh;
		this.tileResolutionPix = tileResolutionPix;
	}


	/**
	 * Build the tiles for several tile sizes.
	 * 
	 * @param minPowTwo
	 * @param maxPowTwo
	 */
	public void createTiles(boolean createEmptyTiles) {
		//create tile dictionnary tileId -> tile
		HashMap<String,GridStatTile> tiles_ = new HashMap<String,GridStatTile>();

		//go through cell stats and assign it to a tile
		for(Stat s : sh.stats) {
			//get cell information
			String gridId = s.dims.get(gridIdAtt);
			GridCell cell = new GridCell(gridId);
			double x = cell.getLowerLeftCornerPositionX();
			double y = cell.getLowerLeftCornerPositionY();
			int resolution = cell.getResolution();


			//compute tile size, in geo unit
			int tileSizeM = resolution * this.tileResolutionPix;

			//find tile position
			int xt = (int)( (x-originPoint.x)/tileSizeM );
			int yt = (int)( (y-originPoint.y)/tileSizeM );

			//get tile. If it does not exists, create it.
			String tileId = xt+"_"+yt;
			GridStatTile tile = tiles_.get(tileId);
			if(tile == null) {
				tile = new GridStatTile(xt, yt);
				tiles_.put(tileId, tile);
			}

			//add cell to tile
			tile.stats.add(s);
		}

		/*tilesInfo = null;
		if(createEmptyTiles) {
			bn = getTilesInfo().bounds.
					for(int x=getTilesInfo().bounds.);
							//TODO
		}*/

		tiles = tiles_.values();
	}


	/**
	 * Save the tile pyramid as CSV.
	 * 
	 * @param folderPath
	 */
	public void saveCSV(String folderPath) {

		for(GridStatTile t : tiles) {
			//build sh for the tile
			StatsHypercube sht = new StatsHypercube(sh.getDimLabels());
			sht.dimLabels.add("x");
			sht.dimLabels.add("y");
			sht.dimLabels.remove(gridIdAtt);

			//prepare tile stats for export
			for(Stat s : t.stats) {

				//get cell position
				GridCell cell = new GridCell( s.dims.get(gridIdAtt) );
				double x = cell.getLowerLeftCornerPositionX() - originPoint.x;
				double y = cell.getLowerLeftCornerPositionY() - originPoint.y;
				double r = cell.getResolution();

				//compute cell position in tile space
				x = x/r - t.x*tileResolutionPix;
				y = y/r - t.y*tileResolutionPix;

				/*/check x,y values. Should be within [0,tileSizeCellNb-1]
				if(x==0) System.out.println("x=0 found");
				if(y==0) System.out.println("y=0 found");
				if(x<0) System.err.println("Too low value: "+x);
				if(y<0) System.err.println("Too low value: "+y);
				if(x==tileSizeCellNb-1) System.out.println("x=tileSizeCellNb-1 found");
				if(y==tileSizeCellNb-1) System.out.println("y=tileSizeCellNb-1 found");
				if(x>tileSizeCellNb-1) System.err.println("Too high value: "+x);
				if(y>tileSizeCellNb-1) System.err.println("Too high value: "+y);*/

				//store value
				Stat s_ = new Stat(s.value, "x", ""+(int)x, "y", ""+(int)y);
				sht.stats.add(s_);
			}

			//TODO empty tiles: make empty file

			//save as csv file
			//TODO be sure order is x,y,val
			//TODO handle case of more columns, when using multidimensional stats
			//TODO add json with service information
			CSV.save(sht, "val", folderPath + "/" +t.x+ "/" +t.y+ ".csv");
		}
	}




	TilesInfo tilesInfo = null;
	public TilesInfo getTilesInfo() {
		if (tilesInfo == null)
			computeTilesInfo();
		return tilesInfo;
	}

	class TilesInfo {
		public String description;
		Envelope bounds = null;
		public int resolution = -1;
		public String ePSGCode;
		public double minStatValue = Double.MAX_VALUE, maxStatValue = -Double.MAX_VALUE;
		public double[] percentiles;
		public double[] percentilesNoZero;
	}

	private TilesInfo computeTilesInfo() {
		tilesInfo = new TilesInfo();
		Collection<Double> vals = new ArrayList<>();
		Collection<Double> valsNoZero = new ArrayList<>();

		for(GridStatTile t : getTiles()) {
			//set x/y envelope
			if(tilesInfo.bounds==null) tilesInfo.bounds = new Envelope(new Coordinate(t.x, t.y));
			else tilesInfo.bounds.expandToInclude(t.x, t.y);

			//set resolution and CRS
			if(tilesInfo.resolution == -1 && t.stats.size()>0) {
				GridCell cell = new GridCell( t.stats.get(0).dims.get(gridIdAtt) );
				tilesInfo.resolution = cell.getResolution();
				tilesInfo.ePSGCode = cell.getEpsgCode();
			}

			//set min/max stat values
			if(t.stats.size()>0) {
				tilesInfo.maxStatValue = Math.max(t.getMaxValue().value, tilesInfo.maxStatValue);
				tilesInfo.minStatValue = Math.min(t.getMinValue().value, tilesInfo.minStatValue);
			}

			//store values
			for(Stat s : t.stats) {
				vals.add(s.value);
				if(s.value !=0) valsNoZero.add(s.value);
			}

		}

		tilesInfo.percentiles = StatsUtil.getQuantiles(vals, 99);
		tilesInfo.percentilesNoZero = StatsUtil.getQuantiles(valsNoZero, 99);

		return tilesInfo;
	}

}
