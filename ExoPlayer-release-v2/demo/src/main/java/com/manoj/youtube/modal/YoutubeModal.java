package com.manoj.youtube.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj on 11/01/2016.
 */
public class YoutubeModal {

    @SerializedName("kind")
    @Expose
    private String kind;
    @SerializedName("etag")
    @Expose
    private String etag;
    @SerializedName("nextPageToken")
    @Expose
    private String nextPageToken;
    @SerializedName("regionCode")
    @Expose
    private String regionCode;
    @SerializedName("pageInfo")
    @Expose
    private Item.High.PageInfo pageInfo;
    @SerializedName("items")
    @Expose
    private List<Item> items = new ArrayList<Item>();

    /**
     * @return The kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * @param kind The kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * @return The etag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * @param etag The etag
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * @return The nextPageToken
     */
    public String getNextPageToken() {
        return nextPageToken;
    }

    /**
     * @param nextPageToken The nextPageToken
     */
    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    /**
     * @return The regionCode
     */
    public String getRegionCode() {
        return regionCode;
    }

    /**
     * @param regionCode The regionCode
     */
    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    /**
     * @return The pageInfo
     */
    public Item.High.PageInfo getPageInfo() {
        return pageInfo;
    }

    /**
     * @param pageInfo The pageInfo
     */
    public void setPageInfo(Item.High.PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    /**
     * @return The items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * @param items The items
     */
    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item {

        @SerializedName("kind")
        @Expose
        private String kind;
        @SerializedName("etag")
        @Expose
        private String etag;
        @SerializedName("id")
        @Expose
        private High.Id id;
        @SerializedName("snippet")
        @Expose
        private High.Snippet snippet;

        /**
         * @return The kind
         */
        public String getKind() {
            return kind;
        }

        /**
         * @param kind The kind
         */
        public void setKind(String kind) {
            this.kind = kind;
        }

        /**
         * @return The etag
         */
        public String getEtag() {
            return etag;
        }

        /**
         * @param etag The etag
         */
        public void setEtag(String etag) {
            this.etag = etag;
        }

        /**
         * @return The id
         */
        public High.Id getId() {
            return id;
        }

        /**
         * @param id The id
         */
        public void setId(High.Id id) {
            this.id = id;
        }

        /**
         * @return The snippet
         */
        public High.Snippet getSnippet() {
            return snippet;
        }

        /**
         * @param snippet The snippet
         */
        public void setSnippet(High.Snippet snippet) {
            this.snippet = snippet;
        }


        public class High {

            @SerializedName("url")
            @Expose
            private String url;
            @SerializedName("width")
            @Expose
            private Integer width;
            @SerializedName("height")
            @Expose
            private Integer height;

            /**
             * @return The url
             */
            public String getUrl() {
                return url;
            }

            /**
             * @param url The url
             */
            public void setUrl(String url) {
                this.url = url;
            }

            /**
             * @return The width
             */
            public Integer getWidth() {
                return width;
            }

            /**
             * @param width The width
             */
            public void setWidth(Integer width) {
                this.width = width;
            }

            /**
             * @return The height
             */
            public Integer getHeight() {
                return height;
            }

            /**
             * @param height The height
             */
            public void setHeight(Integer height) {
                this.height = height;
            }


            public class Id {

                @SerializedName("kind")
                @Expose
                private String kind;
                @SerializedName("videoId")
                @Expose
                private String videoId;

                /**
                 * @return The kind
                 */
                public String getKind() {
                    return kind;
                }

                /**
                 * @param kind The kind
                 */
                public void setKind(String kind) {
                    this.kind = kind;
                }

                /**
                 * @return The videoId
                 */
                public String getVideoId() {
                    return videoId;
                }

                /**
                 * @param videoId The videoId
                 */
                public void setVideoId(String videoId) {
                    this.videoId = videoId;
                }

            }


            public class Medium {

                @SerializedName("url")
                @Expose
                private String url;
                @SerializedName("width")
                @Expose
                private Integer width;
                @SerializedName("height")
                @Expose
                private Integer height;

                /**
                 * @return The url
                 */
                public String getUrl() {
                    return url;
                }

                /**
                 * @param url The url
                 */
                public void setUrl(String url) {
                    this.url = url;
                }

                /**
                 * @return The width
                 */
                public Integer getWidth() {
                    return width;
                }

                /**
                 * @param width The width
                 */
                public void setWidth(Integer width) {
                    this.width = width;
                }

                /**
                 * @return The height
                 */
                public Integer getHeight() {
                    return height;
                }

                /**
                 * @param height The height
                 */
                public void setHeight(Integer height) {
                    this.height = height;
                }

            }

            public class PageInfo {

                @SerializedName("totalResults")
                @Expose
                private Integer totalResults;
                @SerializedName("resultsPerPage")
                @Expose
                private Integer resultsPerPage;

                /**
                 * @return The totalResults
                 */
                public Integer getTotalResults() {
                    return totalResults;
                }

                /**
                 * @param totalResults The totalResults
                 */
                public void setTotalResults(Integer totalResults) {
                    this.totalResults = totalResults;
                }

                /**
                 * @return The resultsPerPage
                 */
                public Integer getResultsPerPage() {
                    return resultsPerPage;
                }

                /**
                 * @param resultsPerPage The resultsPerPage
                 */
                public void setResultsPerPage(Integer resultsPerPage) {
                    this.resultsPerPage = resultsPerPage;
                }

            }

            public class Snippet {

                @SerializedName("publishedAt")
                @Expose
                private String publishedAt;
                @SerializedName("channelId")
                @Expose
                private String channelId;
                @SerializedName("title")
                @Expose
                private String title;
                @SerializedName("description")
                @Expose
                private String description;
                @SerializedName("thumbnails")
                @Expose
                private Thumbnails thumbnails;
                @SerializedName("channelTitle")
                @Expose
                private String channelTitle;
                @SerializedName("liveBroadcastContent")
                @Expose
                private String liveBroadcastContent;

                /**
                 * @return The publishedAt
                 */
                public String getPublishedAt() {
                    return publishedAt;
                }

                /**
                 * @param publishedAt The publishedAt
                 */
                public void setPublishedAt(String publishedAt) {
                    this.publishedAt = publishedAt;
                }

                /**
                 * @return The channelId
                 */
                public String getChannelId() {
                    return channelId;
                }

                /**
                 * @param channelId The channelId
                 */
                public void setChannelId(String channelId) {
                    this.channelId = channelId;
                }

                /**
                 * @return The title
                 */
                public String getTitle() {
                    return title;
                }

                /**
                 * @param title The title
                 */
                public void setTitle(String title) {
                    this.title = title;
                }

                /**
                 * @return The description
                 */
                public String getDescription() {
                    return description;
                }

                /**
                 * @param description The description
                 */
                public void setDescription(String description) {
                    this.description = description;
                }

                /**
                 * @return The thumbnails
                 */
                public Thumbnails getThumbnails() {
                    return thumbnails;
                }

                /**
                 * @param thumbnails The thumbnails
                 */
                public void setThumbnails(Thumbnails thumbnails) {
                    this.thumbnails = thumbnails;
                }

                /**
                 * @return The channelTitle
                 */
                public String getChannelTitle() {
                    return channelTitle;
                }

                /**
                 * @param channelTitle The channelTitle
                 */
                public void setChannelTitle(String channelTitle) {
                    this.channelTitle = channelTitle;
                }

                /**
                 * @return The liveBroadcastContent
                 */
                public String getLiveBroadcastContent() {
                    return liveBroadcastContent;
                }

                /**
                 * @param liveBroadcastContent The liveBroadcastContent
                 */
                public void setLiveBroadcastContent(String liveBroadcastContent) {
                    this.liveBroadcastContent = liveBroadcastContent;
                }

            }

            public class Thumbnails {

                @SerializedName("default")
                @Expose
                private Default _default;
                @SerializedName("medium")
                @Expose
                private Medium medium;
                @SerializedName("high")
                @Expose
                private High high;

                /**
                 * @return The _default
                 */
                public Default getDefault() {
                    return _default;
                }

                /**
                 * @param _default The default
                 */
                public void setDefault(Default _default) {
                    this._default = _default;
                }

                /**
                 * @return The medium
                 */
                public Medium getMedium() {
                    return medium;
                }

                /**
                 * @param medium The medium
                 */
                public void setMedium(Medium medium) {
                    this.medium = medium;
                }

                /**
                 * @return The high
                 */
                public High getHigh() {
                    return high;
                }

                /**
                 * @param high The high
                 */
                public void setHigh(High high) {
                    this.high = high;
                }

            }

            public class Default {

                @SerializedName("url")
                @Expose
                private String url;
                @SerializedName("width")
                @Expose
                private Integer width;
                @SerializedName("height")
                @Expose
                private Integer height;

                /**
                 * @return The url
                 */
                public String getUrl() {
                    return url;
                }

                /**
                 * @param url The url
                 */
                public void setUrl(String url) {
                    this.url = url;
                }

                /**
                 * @return The width
                 */
                public Integer getWidth() {
                    return width;
                }

                /**
                 * @param width The width
                 */
                public void setWidth(Integer width) {
                    this.width = width;
                }

                /**
                 * @return The height
                 */
                public Integer getHeight() {
                    return height;
                }

                /**
                 * @param height The height
                 */
                public void setHeight(Integer height) {
                    this.height = height;
                }

            }
        }
    }
}



