package si.um.feri.sloventure.data;

import java.util.List;

public class AttractionData {
    public String id;
    public String name;
    public String regionId;
    public String regionName;
    public float lat;
    public float lon;
    public String street;
    public String city;
    public String postalCode;
    public String country;
    public String description;
    public String classification;
    public String locationType;
    public int elevation;
    public float rating;
    public List<AttractionImage> images;

    public AttractionData(
        String id,
        String name,
        String regionId,
        String regionName,
        float lat,
        float lon,
        String street,
        String city,
        String postalCode,
        String country,
        String description,
        String classification,
        String locationType,
        int elevation,
        float rating,
        List<AttractionImage> images
    ) {
        this.id = id;
        this.name = name;
        this.regionId = regionId;
        this.regionName = regionName;
        this.lat = lat;
        this.lon = lon;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.description = description;
        this.classification = classification;
        this.locationType = locationType;
        this.elevation = elevation;
        this.rating = rating;
        this.images = images;
    }
}
