# Grid maker

**NB:** To setup a coding environment, follow the instructions [here](https://github.com/eurostat/README/blob/master/docs/howto/java_eclipse_maven_git_quick_guide.md) and [there](https://github.com/eurostat/JGiscoTools#setup).

To create a 10m resolution grid over 1km² starting at point (0,0):

```java

Grid grid = new Grid()
		.setResolution(10)
		.setGeometryToCover(new Envelope(0, 1000, 0, 1000));

Collection<Feature> cells = grid.getCells();
```

This other example creates a 5km resolution grid covering Luxembourg (code LU) and a 1km margin, based on the European ETRS89-LAEA coordinate reference system ([EPSG:3035](https://spatialreference.org/ref/epsg/etrs89-etrs-laea/)). The cells are saved as a **.shp* file:

```java
//get country geometry
Geometry cntGeom = CountriesUtil.getEuropeanCountry("LU", true).getDefaultGeometry();

//build cells
Grid grid = new Grid()
		.setResolution(5000)
		.setEPSGCode("3035")
		.setGeometryToCover(cntGeom)
		.setToleranceDistance(1000);

//save cells as GeoPackage and SHP file
GeoData.save(grid.getCells(), "path_to_my/file.gpkg", CRS.decode("EPSG:3035"));
GeoData.save(grid.getCells(), "path_to_my/file.shp", CRS.decode("EPSG:3035"));
```

Input geometries can be loaded from [*GeoPackage*](https://www.geopackage.org/), [*Shapefile*](https://en.wikipedia.org/wiki/Shapefile) or [*GeoJSON*](https://geojson.org/) files or simply specified as rectangular extent. The grid cell geometries can be squared surfaces or points located at the center of these cells. Each grid cell is identified with a standard code such as *CRS3035RES200mN1453400E1452800*. The output grid cells can be saved as [*GeoPackage*](https://www.geopackage.org/), [*Shapefile*](https://en.wikipedia.org/wiki/Shapefile) or [*GeoJSON*](https://geojson.org/) files.

## Documentation

See the [Javadoc API](https://eurostat.github.io/JGiscoTools/doc/site/apidocs/eu/europa/ec/eurostat/jgiscotools/grid/package-summary.html).

## Use it as program

See [GridMaker](https://github.com/eurostat/GridMaker).
