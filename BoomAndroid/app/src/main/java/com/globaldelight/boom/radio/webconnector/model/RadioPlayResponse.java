package com.globaldelight.boom.radio.webconnector.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 11-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class RadioPlayResponse {
    @SerializedName("message")
    @Expose
    private List<Object> message = null;
    @SerializedName("body")
    @Expose
    private Body body;

    public List<Object> getMessage() {
        return message;
    }

    public void setMessage(List<Object> message) {
        this.message = message;
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
        @SerializedName("content")
        @Expose
        private Content content;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }

    }

    public class Content {

        @SerializedName("services")
        @Expose
        private Services services;
        @SerializedName("streams")
        @Expose
        private List<Stream> streams = null;
        @SerializedName("type")
        @Expose
        private String type;

        public Services getServices() {
            return services;
        }

        public void setServices(Services services) {
            this.services = services;
        }

        public List<Stream> getStreams() {
            return streams;
        }

        public void setStreams(List<Stream> streams) {
            this.streams = streams;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }


    public class Services {


    }

    public class Stream {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("url")
        @Expose
        private String url;
        @SerializedName("protocol")
        @Expose
        private String protocol;
        @SerializedName("port")
        @Expose
        private Integer port;
        @SerializedName("format")
        @Expose
        private String format;
        @SerializedName("codec")
        @Expose
        private String codec;
        @SerializedName("bitrate")
        @Expose
        private Integer bitrate;
        @SerializedName("frequency")
        @Expose
        private Integer frequency;
        @SerializedName("channels")
        @Expose
        private Integer channels;
        @SerializedName("isStereo")
        @Expose
        private Boolean isStereo;
        @SerializedName("isShoutcast")
        @Expose
        private Boolean isShoutcast;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getCodec() {
            return codec;
        }

        public void setCodec(String codec) {
            this.codec = codec;
        }

        public Integer getBitrate() {
            return bitrate;
        }

        public void setBitrate(Integer bitrate) {
            this.bitrate = bitrate;
        }

        public Integer getFrequency() {
            return frequency;
        }

        public void setFrequency(Integer frequency) {
            this.frequency = frequency;
        }

        public Integer getChannels() {
            return channels;
        }

        public void setChannels(Integer channels) {
            this.channels = channels;
        }

        public Boolean getIsStereo() {
            return isStereo;
        }

        public void setIsStereo(Boolean isStereo) {
            this.isStereo = isStereo;
        }

        public Boolean getIsShoutcast() {
            return isShoutcast;
        }

        public void setIsShoutcast(Boolean isShoutcast) {
            this.isShoutcast = isShoutcast;
        }

    }
}
