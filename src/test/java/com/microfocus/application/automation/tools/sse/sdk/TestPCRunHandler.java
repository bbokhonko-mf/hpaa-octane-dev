/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.sse.sdk;

import com.microfocus.application.automation.tools.rest.RestClient;
import com.microfocus.application.automation.tools.sse.common.RestClient4Test;
import com.microfocus.application.automation.tools.sse.common.TestCase;
import com.microfocus.application.automation.tools.sse.sdk.handler.RunHandler;
import com.microfocus.application.automation.tools.sse.sdk.handler.RunHandlerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 */
@SuppressWarnings({"squid:S2699","squid:S3658"})
public class TestPCRunHandler extends TestCase {

    @Test
    public void testStart() {

        Client client = new MockRestStartClient(URL, DOMAIN, PROJECT, USER);
        Response response =
                new RunHandlerFactory().create(client, "PC", ENTITY_ID).start(
                        DURATION,
                        POST_RUN_ACTION,
                        "",
                        null);
        Assert.assertTrue(response.isOk());
    }

    private class MockRestStartClient extends RestClient4Test {

        public MockRestStartClient(String url, String domain, String project, String username) {

            super(url, domain, project, username);
        }

        @Override
        public Response httpPost(String url, byte[] data, Map<String, String> headers, ResourceAccessLevel resourceAccessLevel) {

            return new Response(null, null, null, HttpURLConnection.HTTP_OK);
        }
    }

    @Test
    public void testStop() {

        Client client = new MockRestStopClient(URL, DOMAIN, PROJECT, USER);
        Response response = new RunHandlerFactory().create(client, "PC", "23").stop();
        Assert.assertTrue(response.isOk());
    }

    @Test
    public void testReportUrl() {
        RunHandler handler =
                new RunHandlerFactory().create(
                        new RestClient(URL, DOMAIN, PROJECT, USER),
                        "PC",
                        "1001");
        handler.setRunId("1");

        try {
            Assert.assertTrue(String.format(
                    "td://%s.%s.%s:8080/qcbin/[TestRuns]?EntityLogicalName=run&EntityID=%s",
                    PROJECT,
                    DOMAIN,
                    new URL(URL).getHost(),
                    "1").equals(
                    handler.getReportUrl(createArgs())));
        } catch (MalformedURLException e) {
            Assert.fail();
        }

    }

    private class MockRestStopClient extends RestClient4Test {

        public MockRestStopClient(String url, String domain, String project, String username) {

            super(url, domain, project, username);
        }

        @Override
        public Response httpPost(String url, byte[] data, Map<String, String> headers, ResourceAccessLevel resourceAccessLevel) {

            return new Response(null, null, null, HttpURLConnection.HTTP_OK);
        }
    }
}