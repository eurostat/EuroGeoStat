package eu.europa.ec.eurostat.jgiscotools.geocoding.base;

import eu.europa.ec.eurostat.jgiscotools.deprecated.NUTSUtils;

public class GeocodingAddress {

	public String street;

	public String housenumber;
	public String streetname;

	public String city;
	public String countryCode;
	public String postalcode;

	public GeocodingAddress(String street, String housenumber, String streetname, String city, String countryCode, String postalcode) {
		this.street = street;
		this.housenumber = housenumber;
		this.streetname = streetname;
		this.city = city;
		this.countryCode = countryCode;
		this.postalcode = postalcode;
	}

	public String getCountryName() {
		switch (this.countryCode) {
		case "GP": return "Guadeloupe";
		case "MQ": return "Martinique";
		case "GF": return "Guyane française";
		case "RE": return "Réunion";
		case "PM": return "Saint-Pierre et Miquelon";
		case "YT": return "Mayotte";
		}
		String name = NUTSUtils.getName(this.countryCode);
		name = name.replace(" (until 1990 former territory of the FRG)", "");
		return name;
	}

}
