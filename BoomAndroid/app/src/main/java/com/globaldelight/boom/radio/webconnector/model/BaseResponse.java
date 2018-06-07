package com.globaldelight.boom.radio.webconnector.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by adarsh on 07/06/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class BaseResponse<T> {

    @SerializedName("body")
    @Expose
    private Body<T> body;

    public Body<T> getBody() {
        return body;
    }

    public static class Body <T> {
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
        private List<T> content = null;

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

        public List<T> getContent() {
            return content;
        }


    }

}
