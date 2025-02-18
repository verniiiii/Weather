import com.google.gson.annotations.SerializedName

data class YandexGeocoderResponse(
    val response: Response
)

data class Response(
    @SerializedName("GeoObjectCollection")
    val geoObjectCollection: GeoObjectCollection
)

data class GeoObjectCollection(
    val featureMember: List<FeatureMember>
)

data class FeatureMember(
    @SerializedName("GeoObject")
    val geoObject: GeoObject
)

data class GeoObject(
    val metaDataProperty: GeoObjectMetaDataProperty,
    val name: String
)

data class GeoObjectMetaDataProperty(
    @SerializedName("GeocoderMetaData")
    val geocoderMetaData: GeocoderMetaData
)

data class GeocoderMetaData(
    val text: String,
    val kind: String
)