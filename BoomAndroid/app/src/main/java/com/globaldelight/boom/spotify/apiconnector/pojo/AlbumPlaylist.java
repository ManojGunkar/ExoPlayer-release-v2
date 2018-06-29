package com.globaldelight.boom.spotify.apiconnector.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 10/26/2017.
 */

public class AlbumPlaylist {

    @SerializedName("album_type")
    @Expose
    private String albumType;
    @SerializedName("artists")
    @Expose
    private List<Artist> artists = null;
    @SerializedName("available_markets")
    @Expose
    private List<Object> availableMarkets = null;
    @SerializedName("copyrights")
    @Expose
    private List<Copyright> copyrights = null;
    @SerializedName("external_ids")
    @Expose
    private ExternalIds externalIds;
    @SerializedName("external_urls")
    @Expose
    private ExternalUrls_ externalUrls;
    @SerializedName("genres")
    @Expose
    private List<Object> genres = null;
    @SerializedName("href")
    @Expose
    private String href;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("images")
    @Expose
    private List<Image> images = null;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("popularity")
    @Expose
    private Integer popularity;
    @SerializedName("release_date")
    @Expose
    private String releaseDate;
    @SerializedName("release_date_precision")
    @Expose
    private String releaseDatePrecision;
    @SerializedName("tracks")
    @Expose
    private Tracks tracks;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("uri")
    @Expose
    private String uri;

    public String getAlbumType() {
        return albumType;
    }

    public void setAlbumType(String albumType) {
        this.albumType = albumType;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    public List<Object> getAvailableMarkets() {
        return availableMarkets;
    }

    public void setAvailableMarkets(List<Object> availableMarkets) {
        this.availableMarkets = availableMarkets;
    }

    public List<Copyright> getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(List<Copyright> copyrights) {
        this.copyrights = copyrights;
    }

    public ExternalIds getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(ExternalIds externalIds) {
        this.externalIds = externalIds;
    }

    public ExternalUrls_ getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(ExternalUrls_ externalUrls) {
        this.externalUrls = externalUrls;
    }

    public List<Object> getGenres() {
        return genres;
    }

    public void setGenres(List<Object> genres) {
        this.genres = genres;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getReleaseDatePrecision() {
        return releaseDatePrecision;
    }

    public void setReleaseDatePrecision(String releaseDatePrecision) {
        this.releaseDatePrecision = releaseDatePrecision;
    }

    public Tracks getTracks() {
        return tracks;
    }

    public void setTracks(Tracks tracks) {
        this.tracks = tracks;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    public class Artist {

        @SerializedName("external_urls")
        @Expose
        private ExternalUrls externalUrls;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("uri")
        @Expose
        private String uri;

        public ExternalUrls getExternalUrls() {
            return externalUrls;
        }

        public void setExternalUrls(ExternalUrls externalUrls) {
            this.externalUrls = externalUrls;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

    }
    public class Artist_ {

        @SerializedName("external_urls")
        @Expose
        private ExternalUrls__ externalUrls;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("uri")
        @Expose
        private String uri;

        public ExternalUrls__ getExternalUrls() {
            return externalUrls;
        }

        public void setExternalUrls(ExternalUrls__ externalUrls) {
            this.externalUrls = externalUrls;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

    }
    public class Copyright {

        @SerializedName("text")
        @Expose
        private String text;
        @SerializedName("type")
        @Expose
        private String type;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }
    public class ExternalIds {

        @SerializedName("upc")
        @Expose
        private String upc;

        public String getUpc() {
            return upc;
        }

        public void setUpc(String upc) {
            this.upc = upc;
        }

    }
    public class ExternalUrls {

        @SerializedName("spotify")
        @Expose
        private String spotify;

        public String getSpotify() {
            return spotify;
        }

        public void setSpotify(String spotify) {
            this.spotify = spotify;
        }

    }
    public class ExternalUrls_ {

        @SerializedName("spotify")
        @Expose
        private String spotify;

        public String getSpotify() {
            return spotify;
        }

        public void setSpotify(String spotify) {
            this.spotify = spotify;
        }

    }
    public class ExternalUrls__ {

        @SerializedName("spotify")
        @Expose
        private String spotify;

        public String getSpotify() {
            return spotify;
        }

        public void setSpotify(String spotify) {
            this.spotify = spotify;
        }

    }
    public class ExternalUrls___ {

        @SerializedName("spotify")
        @Expose
        private String spotify;

        public String getSpotify() {
            return spotify;
        }

        public void setSpotify(String spotify) {
            this.spotify = spotify;
        }

    }
    public class Image {

        @SerializedName("height")
        @Expose
        private Integer height;
        @SerializedName("url")
        @Expose
        private String url;
        @SerializedName("width")
        @Expose
        private Integer width;

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

    }
    public class Item {

        @SerializedName("artists")
        @Expose
        private List<Artist_> artists = null;
        @SerializedName("available_markets")
        @Expose
        private List<Object> availableMarkets = null;
        @SerializedName("disc_number")
        @Expose
        private Integer discNumber;
        @SerializedName("duration_ms")
        @Expose
        private Integer durationMs;
        @SerializedName("explicit")
        @Expose
        private Boolean explicit;
        @SerializedName("external_urls")
        @Expose
        private ExternalUrls___ externalUrls;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("preview_url")
        @Expose
        private Object previewUrl;
        @SerializedName("track_number")
        @Expose
        private Integer trackNumber;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("uri")
        @Expose
        private String uri;

        public List<Artist_> getArtists() {
            return artists;
        }

        public void setArtists(List<Artist_> artists) {
            this.artists = artists;
        }

        public List<Object> getAvailableMarkets() {
            return availableMarkets;
        }

        public void setAvailableMarkets(List<Object> availableMarkets) {
            this.availableMarkets = availableMarkets;
        }

        public Integer getDiscNumber() {
            return discNumber;
        }

        public void setDiscNumber(Integer discNumber) {
            this.discNumber = discNumber;
        }

        public Integer getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(Integer durationMs) {
            this.durationMs = durationMs;
        }

        public Boolean getExplicit() {
            return explicit;
        }

        public void setExplicit(Boolean explicit) {
            this.explicit = explicit;
        }

        public ExternalUrls___ getExternalUrls() {
            return externalUrls;
        }

        public void setExternalUrls(ExternalUrls___ externalUrls) {
            this.externalUrls = externalUrls;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getPreviewUrl() {
            return previewUrl;
        }

        public void setPreviewUrl(Object previewUrl) {
            this.previewUrl = previewUrl;
        }

        public Integer getTrackNumber() {
            return trackNumber;
        }

        public void setTrackNumber(Integer trackNumber) {
            this.trackNumber = trackNumber;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

    }
    public class Tracks {

        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("items")
        @Expose
        private List<Item> items = null;
        @SerializedName("limit")
        @Expose
        private Integer limit;
        @SerializedName("next")
        @Expose
        private Object next;
        @SerializedName("offset")
        @Expose
        private Integer offset;
        @SerializedName("previous")
        @Expose
        private Object previous;
        @SerializedName("total")
        @Expose
        private Integer total;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Object getNext() {
            return next;
        }

        public void setNext(Object next) {
            this.next = next;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Object getPrevious() {
            return previous;
        }

        public void setPrevious(Object previous) {
            this.previous = previous;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

    }

}
