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

package com.microfocus.application.automation.tools.octane.actions.plugin;

import com.gargoylesoftware.htmlunit.Page;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.octane.integrations.dto.general.CIServerTypes;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.microfocus.application.automation.tools.octane.OctanePluginTestBase;
import com.microfocus.application.automation.tools.octane.actions.PluginActions;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.tests.TestUtils;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 07/01/15
 * Time: 22:09
 * To change this template use File | Settings | File Templates.
 */

@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872", "squid:S2925", "squid:S109", "squid:S1607", "squid:S2701", "squid:S3578", "squid:S2698"})
public class PluginActionsTest extends OctanePluginTestBase {

	@Test
	public void testPluginActionsMethods() {
		PluginActions pluginActions = new PluginActions();
		assertNull(pluginActions.getIconFileName());
		assertNull(pluginActions.getDisplayName());
		assertEquals("nga", pluginActions.getUrlName());
	}

	@Ignore("temp ignore")
	@Test
	public void testPluginActions_REST_Status() throws IOException, SAXException {
		Page page = client.goTo("nga/api/v1/status", "application/json");
		System.out.println(page.getWebResponse().getContentAsString());
		CIProviderSummaryInfo status = dtoFactory.dtoFromJson(page.getWebResponse().getContentAsString(), CIProviderSummaryInfo.class);

		assertNotNull(status);

		assertNotNull(status.getServer());
		assertEquals(CIServerTypes.JENKINS.value(), status.getServer().getType());
		assertEquals(Jenkins.VERSION, status.getServer().getVersion());
		assertEquals(rule.getInstance().getRootUrl(), status.getServer().getUrl() + "/");
		assertNotNull(status.getServer().getSendingTime());

		assertNotNull(status.getPlugin());
		assertEquals(ConfigurationService.getPluginVersion(), status.getPlugin().getVersion());
	}

	@Test
	public void testPluginActions_REST_Jobs_NoParams() throws IOException, SAXException {
		String projectName = "root-job-" + UUID.randomUUID().toString();

		String taskUrl = "nga/api/v1/jobs";
		CIJobsList response = TestUtils.sendTask(taskUrl, CIJobsList.class);

		assertNotNull(response);
		assertNotNull(response.getJobs());
		assertEquals(rule.getInstance().getTopLevelItemNames().size(), response.getJobs().length);

		rule.createFreeStyleProject(projectName);
		response = TestUtils.sendTask(taskUrl, CIJobsList.class);

		assertNotNull(response);
		assertNotNull(response.getJobs());
		for (PipelineNode ciJob : response.getJobs()) {
			if (projectName.equals(ciJob.getName())) {
				assertNotNull(ciJob.getParameters());
				assertEquals(0, ciJob.getParameters().size());
			}
		}
	}

	@Test
	public void testPluginActions_REST_Jobs_WithParams() throws IOException, SAXException {
		String projectName = "root-job-" + UUID.randomUUID().toString();
		FreeStyleProject fsp;

		String taskUrl = "nga/api/v1/jobs";
		CIJobsList response = TestUtils.sendTask(taskUrl, CIJobsList.class);

		assertNotNull(response);
		assertNotNull(response.getJobs());
		assertEquals(0, response.getJobs().length);

		fsp = rule.createFreeStyleProject(projectName);
		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string"),
				(ParameterDefinition) new FileParameterDefinition("ParamC", "file param")
		));
		fsp.addProperty(params);
		response = TestUtils.sendTask(taskUrl, CIJobsList.class);

		assertNotNull(response);
		assertNotNull(response.getJobs());
		assertEquals(1, response.getJobs().length);
		assertEquals(projectName, response.getJobs()[0].getName());
		assertNotNull(response.getJobs()[0].getParameters());
		assertEquals(3, response.getJobs()[0].getParameters().size());

		//  Test ParamA
		assertNotNull(response.getJobs()[0].getParameters().get(0));
		assertEquals("ParamA", response.getJobs()[0].getParameters().get(0).getName());
		assertEquals(CIParameterType.BOOLEAN, response.getJobs()[0].getParameters().get(0).getType());
		assertEquals("bool", response.getJobs()[0].getParameters().get(0).getDescription());
		assertEquals(true, response.getJobs()[0].getParameters().get(0).getDefaultValue());

		//  Test ParamB
		assertNotNull(response.getJobs()[0].getParameters().get(1));
		assertEquals("ParamB", response.getJobs()[0].getParameters().get(1).getName());
		assertEquals(CIParameterType.STRING, response.getJobs()[0].getParameters().get(1).getType());
		assertEquals("string", response.getJobs()[0].getParameters().get(1).getDescription());
		assertEquals("str", response.getJobs()[0].getParameters().get(1).getDefaultValue());

		//  Test ParamC
		assertNotNull(response.getJobs()[0].getParameters().get(2));
		assertEquals("ParamC", response.getJobs()[0].getParameters().get(2).getName());
		assertEquals(CIParameterType.FILE, response.getJobs()[0].getParameters().get(2).getType());
		assertEquals("file param", response.getJobs()[0].getParameters().get(2).getDescription());
		assertEquals("", response.getJobs()[0].getParameters().get(2).getDefaultValue());
	}
}
