package com.globaldelight.boom.radio.webconnector.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 11-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class CountryResponse {
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

    }

}
