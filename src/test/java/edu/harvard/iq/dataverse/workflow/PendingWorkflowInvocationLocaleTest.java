package edu.harvard.iq.dataverse.workflow;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.RoleAssigneeServiceBean;
import edu.harvard.iq.dataverse.authorization.groups.impl.ipaddress.ip.IpAddress;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.workflow.step.Pending;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PendingWorkflowInvocationLocaleTest {
    @ParameterizedTest
    @ValueSource(strings = {"uk", "ua", "en"})
    void restoresLocaleAndKeepsInternalMetadataOutOfStepData(String language) {
        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserIdentifier("publisher");
        DataverseRequest request = new DataverseRequest(user, IpAddress.valueOf("127.0.0.1"), Locale.forLanguageTag(language));
        WorkflowContext context = new WorkflowContext(request, new Dataset(), 1L, 0L,
                WorkflowContext.TriggerType.PrePublishDataset, null, null, false, "test-invocation", 1L);
        Map<String, String> originalStepData = Map.of("authorized", "true", "callback", "test");
        PendingWorkflowInvocation pending = new PendingWorkflowInvocation(new Workflow(), context, new Pending(originalStepData));
        RoleAssigneeServiceBean assignees = mock(RoleAssigneeServiceBean.class);
        when(assignees.getRoleAssignee(user.getIdentifier())).thenReturn(user);

        WorkflowContext restored = pending.reCreateContext(assignees);
        assertEquals(Locale.forLanguageTag(language), restored.getRequest().getNotificationLocale());
        assertEquals(originalStepData, pending.getLocalData());
        assertEquals("test-invocation", restored.getInvocationId());
        assertEquals(1L, restored.getLockId());
    }

    @Test
    void oldPendingWorkflowWithoutLocaleKeepsTheExistingFallback() {
        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserIdentifier("publisher");
        RoleAssigneeServiceBean assignees = mock(RoleAssigneeServiceBean.class);
        when(assignees.getRoleAssignee(user.getIdentifier())).thenReturn(user);
        PendingWorkflowInvocation pending = new PendingWorkflowInvocation();
        pending.setUserId(user.getIdentifier());
        pending.setIpAddress("127.0.0.1");
        pending.setLocalData(new HashMap<>(Map.of("authorized", "true")));
        assertNull(pending.reCreateContext(assignees).getRequest().getNotificationLocale());
        assertEquals(Map.of("authorized", "true"), pending.getLocalData());
    }
}
