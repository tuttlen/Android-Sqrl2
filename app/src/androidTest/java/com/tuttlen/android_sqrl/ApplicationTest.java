package com.tuttlen.android_sqrl;

import android.app.Application;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void TestAuth() throws Exception {

        authRequest req = new authRequest("https://localhost/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals(testReuslt, "localhost");
    }
    public void TestAuth2() throws Exception {

        authRequest req = new authRequest("https://localhost/sqrl?4095c8adfa51dabe30fe9f9474d3f91def620300e489e6853baa67bed5d5e0d4");
        String testReuslt = req.getDomain();
        assertEquals(testReuslt,"");
    }

    public void testPostSqrl()
    {
        String privateKey ="9U0eUkrV18ObhG+n7M/DqFlxPaSqytkHwL4RLuXtlkbF8JB2XUxF4Lxj0qpe0SI3aLErphECKU6P+1eKBfqYlw==";
        String publicKey = "xfCQdl1MReC8Y9KqXtEiN2ixK6YRAilOj/tXigX6mJc=";
        MainActivity testActivity = new MainActivity();
        String URL ="sqrl://www.grc.com/sqrl?nut=oh5REYgoyG10VejQYz7pcA&sfn=R1JD";
        byte[] signature =Helper.Sign(URL.getBytes(),Helper.urlDecode(privateKey));
        String message ="sqrl://www.grc.com/sqrl?nut=oh5REYgoyG10VejQYz7pcA&sfn=R1JD";
        testActivity.web_post3(URL,message,Helper.urlEncode(signature),publicKey,Helper.urlDecode(privateKey));
    }
}