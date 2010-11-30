/*
Copyright (c) 2007-2010, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package twitter4j.pics;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.TwitterException;
import twitter4j.http.OAuthAuthorization;
import twitter4j.internal.http.HttpClientWrapper;
import twitter4j.internal.http.HttpParameter;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.logging.Logger;

/**
 * @author Rémy Rakic - remy.rakic at gmail.com
 * @author Takao Nakaguchi - takao.nakaguchi at gmail.com
 * @author withgod - noname at withgod.jp
 * @since Twitter4J 2.1.8
 */
public abstract class AbstractImageUploader implements ImageUploader {
    public static final String TWITTER_VERIFY_CREDENTIALS_JSON = "https://api.twitter.com/1/account/verify_credentials.json";
    public static final String TWITTER_VERIFY_CREDENTIALS_XML  = "https://api.twitter.com/1/account/verify_credentials.xml";
    
    protected String apiKey = null;
    protected OAuthAuthorization oauth = null;
    protected String uploadUrl = null;
    protected HttpParameter[] postParameter = null;
    protected HttpParameter image = null;
    protected HttpParameter message = null;
    protected Map<String, String> headers = new HashMap<String, String>();
    protected HttpResponse httpResponse = null;
    protected static final Logger logger = Logger.getLogger(AbstractImageUploader.class);

    public abstract void preUp() throws TwitterException, ImageUploadException;
    public abstract String postUp() throws TwitterException, ImageUploadException;

    public AbstractImageUploader(OAuthAuthorization oauth) {
        this.oauth = oauth;
    }

    public AbstractImageUploader(String apiKey, OAuthAuthorization oauth) {
        this.apiKey = apiKey;
        this.oauth = oauth;
    }

    public String upload(String imageFileName, InputStream imageBody) throws TwitterException, ImageUploadException {
        this.image = new HttpParameter("media", imageFileName, imageBody);
        return upload();
    }
    public String upload(String imageFileName, InputStream imageBody, String message) throws TwitterException, ImageUploadException {
        this.image = new HttpParameter("media", imageFileName, imageBody);
        this.message = new HttpParameter("message", message);
        return upload();
    }
    public String upload(File file, String message) throws TwitterException, ImageUploadException {
        this.image = new HttpParameter("media", file);
        this.message = new HttpParameter("message", message);
        return upload();
    }
    public String upload(File file) throws TwitterException, ImageUploadException {
        this.image = new HttpParameter("media", file);
        return upload();
    }

    public String upload() throws TwitterException, ImageUploadException {
        preUp();
        if (this.postParameter == null) {
            throw new ImageUploadException("Incomplete implementation. dosnt build postParameter");
        }
        if (this.uploadUrl == null) {
            throw new ImageUploadException("Incomplete implementation. not set uploadUrl");
        }


        HttpClientWrapper client = new HttpClientWrapper();
        httpResponse = client.post(uploadUrl, postParameter, headers);

        String mediaUrl = postUp();
        logger.debug("uploaded url [" + mediaUrl + "]");
        
        return mediaUrl;
    }

    protected HttpParameter[] appendHttpParameters(HttpParameter[] src, HttpParameter[] dst) {
        int srcLen = src.length;
        int dstLen = dst.length;
        HttpParameter[] ret = new HttpParameter[srcLen + dstLen];
        for (int i = 0; i < srcLen; i++) {
            ret[i] = src[i];
        }
        for (int i = 0; i < dstLen; i++) {
            ret[srcLen + i] = dst[i];
        }
        return ret;
    }

    protected String generateVerifyCredentialsAuthorizationHeader(String verifyCredentialsUrl) {
        List<HttpParameter> oauthSignatureParams = oauth.generateOAuthSignatureHttpParams("GET", verifyCredentialsUrl);
        return "OAuth realm=\"http://api.twitter.com/\"," + OAuthAuthorization.encodeParameters(oauthSignatureParams, ",", true);
    }

    protected String generateVerifyCredentialsAuthorizationURL(String verifyCredentialsUrl) {
        List<HttpParameter> oauthSignatureParams = oauth.generateOAuthSignatureHttpParams("GET", verifyCredentialsUrl);
        return verifyCredentialsUrl + "?" + OAuthAuthorization.encodeParameters(oauthSignatureParams);
    }

    
}
