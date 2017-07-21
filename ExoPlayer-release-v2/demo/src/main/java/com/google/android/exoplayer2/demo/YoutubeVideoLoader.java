package com.google.android.exoplayer2.demo;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by adarsh on 03/07/17.
 */

class YoutubeVideoLoader extends AsyncTaskLoader<String> {

    private static class FormatDescription {
        String  url = null;
        int     tag = 0;
        String  quality = null;
    }

    private static final String YOUTUBE_VIDEO_INFO_URL = "http://www.youtube.com/get_video_info?&video_id=";

    private static OkHttpClient client;
    private String videoId;

    public YoutubeVideoLoader(Context context, String urlOrId) {
        super(context);
        if ( urlOrId.startsWith("http")) {
            Uri videoUri = Uri.parse(urlOrId);
            videoId = videoUri.getQueryParameter("v");
        }
        else {
            videoId = new String(urlOrId);
        }

    }


    @Override
    public void deliverResult(String data) {
        super.deliverResult(data);

    }

    @Override
    public String loadInBackground() {
        try {
            String infoURL = YOUTUBE_VIDEO_INFO_URL+videoId;
            String streamMap = getFormatStreamMap(getContentsOfUrl(infoURL));
//            if ( streamMap == null ) {
//                streamMap = getFormatStreamMapFromWebPage(getContentsOfUrl(videoUri.toString()));
//            }

            if ( streamMap != null ) {
                List<FormatDescription> formats = parseFormat(streamMap);
                return chooseVideoURL(formats);
            }

            return null;
        }
        catch (Exception e) {
            return null;

        }
    }


    private String getFormatStreamMap(String data) {
        if ( data == null ) return null;
        try {
            Map<String, String> result = new HashMap<>();
            String[] paramList = data.split("&");
            for ( int i = 0; i < paramList.length; i++ ) {
                String[] pair = paramList[i].split("=");
                if ( pair.length < 2 ) continue;

                if ( pair[0].equals("url_encoded_fmt_stream_map") ) {
                    return URLDecoder.decode(pair[1], "UTF-8");
                }
            }

            return null;
        }
        catch (Exception e) {
            return null;
        }
    }

    private String getFormatStreamMapFromWebPage(String html) {
        try {
            String pattern = "\"url_encoded_fmt_stream_map\"\\s*:\\s*\"([^\"]*)\"";

            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(html);
            if ( m.find() && m.groupCount() > 0) {
                String data = m.group(1);
                return data.replaceAll("\\\\u0026","&");
            }
        }
        catch (Exception e) {

        }

        return null;
    }

    private String getContentsOfUrl(String url) {
        if ( client == null ) {
            client = new OkHttpClient();
        }

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            return body.string();

        }
        catch (IOException e) {
            return null;
        }
    }



    private List<FormatDescription> parseFormat(String data) {
        ArrayList<FormatDescription> entries = new ArrayList<>();
        String[] paramList = data.split("&|,");
        FormatDescription entry = new FormatDescription();
        for ( String param : paramList ) {
            boolean completed = updateFormatDescription(param,entry);
            if ( completed ) {
                entries.add(entry);
                entry = new FormatDescription();
            }
        }
        return entries;
    }

    private boolean updateFormatDescription(String param, FormatDescription entry) {
        String[] pair = param.split("=");
        if ( pair.length < 2 ) return false;

        try {
            switch (pair[0]) {
                case "url":
                    entry.url = URLDecoder.decode(pair[1], "UTF-8");
                    break;

                case "itag":
                    entry.tag = Integer.parseInt(URLDecoder.decode(pair[1], "UTF-8"));
                    break;

                case "quality":
                    entry.quality = URLDecoder.decode(pair[1], "UTF-8");
                    break;

                case "type":
                    break;
            }
        }
        catch (UnsupportedEncodingException e) {
        }

        return entry.url != null && entry.tag != 0 && entry.quality != null;
    }

    private String chooseVideoURL(List<FormatDescription> formats) {
        int preferredFormats[] = {43, 22, 18, 36 };
        for ( int i = 0; i < preferredFormats.length; i++ ) {
            for ( FormatDescription format: formats) {
                if ( format.tag == preferredFormats[i] ) {
                    return format.url;
                }
            }
        }

        return null;
    }
}
