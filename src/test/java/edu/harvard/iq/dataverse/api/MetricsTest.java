package edu.harvard.iq.dataverse.api;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.Metric;
import edu.harvard.iq.dataverse.metrics.MetricsServiceBean;
import edu.harvard.iq.dataverse.metrics.MetricsUtil;
import edu.harvard.iq.dataverse.util.json.JsonUtil;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Variant;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static edu.harvard.iq.dataverse.makedatacount.MakeDataCountUtil.MetricType.VIEWS_TOTAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MetricsTest {
    @ParameterizedTest
    @CsvSource({"false,,", "false,UA,", "false,,uard", "false,UA,uard",
            "true,,", "true,UA,", "true,,uard", "true,UA,uard"})
    void cumulativeMetricsBypassOldCacheThenReuseCorrectedCache(boolean currentMonth, String country, String alias) {
        Metrics api = new Metrics();
        api.metricsSvc = mock(MetricsServiceBean.class);
        api.dataverseSvc = mock(DataverseServiceBean.class);
        Dataverse parent = null;
        if (alias != null) {
            parent = mock(Dataverse.class);
            when(parent.isReleased()).thenReturn(true);
            when(api.dataverseSvc.findByAlias(alias)).thenReturn(parent);
        }
        String month = currentMonth ? MetricsUtil.getCurrentMonth() : "2020-01";
        String normalizedCountry = country == null ? null : "ua";
        String oldName = "MDC-" + VIEWS_TOTAL + (country == null ? "" : "-ua");
        String oldValue = JsonUtil.createObjectBuilder().add(VIEWS_TOTAL.toString(), 0).build().toString();
        JsonObject correctedValue = JsonUtil.createObjectBuilder().add(VIEWS_TOTAL.toString(), 1).build();
        Map<String, String> cache = new HashMap<>();
        cache.put(oldName, oldValue);
        when(api.metricsSvc.returnUnexpiredCacheMonthly(anyString(), eq(month), isNull(), eq(parent)))
                .thenAnswer(call -> cache.get(call.getArgument(0)));
        when(api.metricsSvc.getMDCDatasetMetrics(VIEWS_TOTAL, month, normalizedCountry, parent))
                .thenReturn(correctedValue);
        when(api.metricsSvc.save(any(Metric.class))).thenAnswer(call -> {
            Metric metric = call.getArgument(0);
            assertNotEquals(oldName, metric.getName());
            assertEquals(month, metric.getDateString());
            cache.put(metric.getName(), metric.getValueJson());
            return metric;
        });
        UriInfo uri = uriInfo();
        for (int i = 0; i < 2; i++) {
            Response response = currentMonth
                    ? api.getMakeDataCountMetricCurrentMonth(uri, "viewsTotal", country, alias)
                    : api.getMakeDataCountMetricToMonth(uri, "viewsTotal", month, country, alias);
            assertEquals(200, response.getStatus());
            assertEquals(correctedValue, ((JsonObject) response.getEntity()).getJsonObject("data"));
        }
        verify(api.metricsSvc, times(1)).getMDCDatasetMetrics(VIEWS_TOTAL, month, normalizedCountry, parent);
        verify(api.metricsSvc, times(1)).save(any(Metric.class));
        assertEquals(oldValue, cache.get(oldName));
    }

    @Test
    void monthlyMdcKeepsExistingCacheNamespace() {
        Metrics api = new Metrics();
        api.metricsSvc = mock(MetricsServiceBean.class);
        String name = "MDC-" + VIEWS_TOTAL + "-ua";
        when(api.metricsSvc.returnUnexpiredCacheAllTime(name, null, null))
                .thenReturn("[{\"date\":\"2020-01\",\"count\":1}]");
        Request request = mock(Request.class);
        when(request.selectVariant(anyList())).thenReturn(new Variant(MediaType.APPLICATION_JSON_TYPE, (String) null, null));
        Response response = api.getMakeDataCountMetricTimeSeries(request, uriInfo(), "viewsTotal", "UA", null);
        assertEquals(200, response.getStatus());
        assertEquals(1, ((JsonObject) response.getEntity()).getJsonArray("data").getJsonObject(0).getInt("count"));
        verify(api.metricsSvc).returnUnexpiredCacheAllTime(name, null, null);
        verifyNoMoreInteractions(api.metricsSvc);
    }

    private UriInfo uriInfo() {
        UriInfo uri = mock(UriInfo.class);
        when(uri.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        return uri;
    }
}
