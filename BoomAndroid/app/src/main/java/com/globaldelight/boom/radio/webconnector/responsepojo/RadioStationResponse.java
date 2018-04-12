package com.globaldelight.boom.radio.webconnector.responsepojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class RadioStationResponse {


    @SerializedName("message")
    @Expose
    private List<Object> message = null;
    @SerializedName("path")
    @Expose
    private List<Path> path = null;
    @SerializedName("body")
    @Expose
    private Body body;

    public List<Object> getMessage() {
        return message;
    }

    public void setMessage(List<Object> message) {
        this.message = message;
    }

    public List<Path> getPath() {
        return path;
    }

    public void setPath(List<Path> path) {
        this.path = path;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public class Body {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("page")
        @Expose
        private Integer page;
        @SerializedName("totalPages")
        @Expose
        private Integer totalPages;
        @SerializedName("totalResults")
        @Expose
        private Integer totalResults;
        @SerializedName("content")
        @Expose
        private List<Content> content = null;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public Integer getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(Integer totalResults) {
            this.totalResults = totalResults;
        }

        public List<Content> getContent() {
            return content;
        }

        public void setContent(List<Content> content) {
            this.content = content;
        }

    }

    public class Content {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("permalink")
        @Expose
        private String permalink;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("baseline")
        @Expose
        private String baseline;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("logo")
        @Expose
        private String logo;
        @SerializedName("smallLogo")
        @Expose
        private String smallLogo;
        @SerializedName("country")
        @Expose
        private String country;
        @SerializedName("timezone")
        @Expose
        private String timezone;
        @SerializedName("language")
        @Expose
        private String language;
        @SerializedName("web")
        @Expose
        private String web;
        @SerializedName("social")
        @Expose
        private Social social;
        @SerializedName("techRating")
        @Expose
        private Integer techRating;
        @SerializedName("tags")
        @Expose
        private List<Tag> tags = null;
        @SerializedName("monitoring")
        @Expose
        private Monitoring monitoring;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPermalink() {
            return permalink;
        }

        public void setPermalink(String permalink) {
            this.permalink = permalink;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBaseline() {
            return baseline;
        }

        public void setBaseline(String baseline) {
            this.baseline = baseline;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getSmallLogo() {
            return smallLogo;
        }

        public void setSmallLogo(String smallLogo) {
            this.smallLogo = smallLogo;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getWeb() {
            return web;
        }

        public void setWeb(String web) {
            this.web = web;
        }

        public Social getSocial() {
            return social;
        }

        public void setSocial(Social social) {
            this.social = social;
        }

        public Integer getTechRating() {
            return techRating;
        }

        public void setTechRating(Integer techRating) {
            this.techRating = techRating;
        }

        public List<Tag> getTags() {
            return tags;
        }

        public void setTags(List<Tag> tags) {
            this.tags = tags;
        }

        public Monitoring getMonitoring() {
            return monitoring;
        }

        public void setMonitoring(Monitoring monitoring) {
            this.monitoring = monitoring;
        }

    }

    public class Error {

        @SerializedName("short")
        @Expose
        private String _short;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("long")
        @Expose
        private String _long;

        public String getShort() {
            return _short;
        }

        public void setShort(String _short) {
            this._short = _short;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLong() {
            return _long;
        }

        public void setLong(String _long) {
            this._long = _long;
        }

    }

    public class Monitoring {

        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("error")
        @Expose
        private Error error;
        @SerializedName("reason")
        @Expose
        private Reason reason;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Error getError() {
            return error;
        }

        public void setError(Error error) {
            this.error = error;
        }

        public Reason getReason() {
            return reason;
        }

        public void setReason(Reason reason) {
            this.reason = reason;
        }

    }

    public class Path {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("permalink")
        @Expose
        private String permalink;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("childCount")
        @Expose
        private Integer childCount;
        @SerializedName("productCount")
        @Expose
        private Integer productCount;
        @SerializedName("logo")
        @Expose
        private String logo;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPermalink() {
            return permalink;
        }

        public void setPermalink(String permalink) {
            this.permalink = permalink;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getChildCount() {
            return childCount;
        }

        public void setChildCount(Integer childCount) {
            this.childCount = childCount;
        }

        public Integer getProductCount() {
            return productCount;
        }

        public void setProductCount(Integer productCount) {
            this.productCount = productCount;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

    }

    public class Reason {

        @SerializedName("short")
        @Expose
        private String _short;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("long")
        @Expose
        private String _long;

        public String getShort() {
            return _short;
        }

        public void setShort(String _short) {
            this._short = _short;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLong() {
            return _long;
        }

        public void setLong(String _long) {
            this._long = _long;
        }

    }

    public class Social {

        @SerializedName("facebook")
        @Expose
        private String facebook;
        @SerializedName("googleplus")
        @Expose
        private String googleplus;
        @SerializedName("instagram")
        @Expose
        private String instagram;
        @SerializedName("twitter_account")
        @Expose
        private String twitterAccount;
        @SerializedName("website")
        @Expose
        private String website;
        @SerializedName("youtube")
        @Expose
        private String youtube;
        @SerializedName("pinterest")
        @Expose
        private String pinterest;

        public String getFacebook() {
            return facebook;
        }

        public void setFacebook(String facebook) {
            this.facebook = facebook;
        }

        public String getGoogleplus() {
            return googleplus;
        }

        public void setGoogleplus(String googleplus) {
            this.googleplus = googleplus;
        }

        public String getInstagram() {
            return instagram;
        }

        public void setInstagram(String instagram) {
            this.instagram = instagram;
        }

        public String getTwitterAccount() {
            return twitterAccount;
        }

        public void setTwitterAccount(String twitterAccount) {
            this.twitterAccount = twitterAccount;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getYoutube() {
            return youtube;
        }

        public void setYoutube(String youtube) {
            this.youtube = youtube;
        }

        public String getPinterest() {
            return pinterest;
        }

        public void setPinterest(String pinterest) {
            this.pinterest = pinterest;
        }

    }

    public class Tag {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("permalink")
        @Expose
        private String permalink;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("tagType")
        @Expose
        private String tagType;
        @SerializedName("searchName")
        @Expose
        private String searchName;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPermalink() {
            return permalink;
        }

        public void setPermalink(String permalink) {
            this.permalink = permalink;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTagType() {
            return tagType;
        }

        public void setTagType(String tagType) {
            this.tagType = tagType;
        }

        public String getSearchName() {
            return searchName;
        }

        public void setSearchName(String searchName) {
            this.searchName = searchName;
        }

    }
}
