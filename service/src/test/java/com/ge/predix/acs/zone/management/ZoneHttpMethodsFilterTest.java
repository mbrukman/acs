package com.ge.predix.acs.zone.management;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class ZoneHttpMethodsFilterTest {

    @InjectMocks
    private ZoneController zoneController;

    private static final Set<HttpMethod> ALL_HTTP_METHODS = new HashSet<>(Arrays.asList(HttpMethod.values()));

    private MockMvc mockMvc;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc =
                MockMvcBuilders.standaloneSetup(this.zoneController).addFilters(new ZoneHttpMethodsFilter()).build();
    }

    @Test(dataProvider = "urisAndTheirAllowedHttpMethods")
    public void testUriPatternsAndTheirAllowedHttpMethods(final String uri, final Set<HttpMethod> allowedHttpMethods)
            throws Exception {
        Set<HttpMethod> disallowedHttpMethods = new HashSet<>(ALL_HTTP_METHODS);
        disallowedHttpMethods.removeAll(allowedHttpMethods);
        for (HttpMethod disallowedHttpMethod : disallowedHttpMethods) {
            this.mockMvc.perform(MockMvcRequestBuilders.request(disallowedHttpMethod, URI.create(uri)))
                    .andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
        }
    }

    @DataProvider
    public Object[][] urisAndTheirAllowedHttpMethods() {
        return new Object[][] { new Object[] { "/v1/zone/foo", new HashSet<>(Arrays.asList(HttpMethod.PUT,
                HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS)) } };
    }
}