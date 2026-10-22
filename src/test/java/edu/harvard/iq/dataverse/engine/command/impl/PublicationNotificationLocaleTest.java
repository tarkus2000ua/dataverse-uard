package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.*;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.groups.impl.ipaddress.ip.IpAddress;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PublicationNotificationLocaleTest {
    @Test
    void finalizationForwardsPublishersLocaleToEachEligibleRecipient() {
        AuthenticatedUser publisher = new AuthenticatedUser();
        publisher.setUserIdentifier("publisher");
        AuthenticatedUser recipient = new AuthenticatedUser();
        recipient.setUserIdentifier("recipient");
        Locale locale = Locale.forLanguageTag("uk");
        DataverseRequest request = new DataverseRequest(publisher, IpAddress.valueOf("127.0.0.1"), locale);
        Dataset dataset = mock(Dataset.class, RETURNS_DEEP_STUBS);
        when(dataset.getLatestVersion().getId()).thenReturn(42L);
        CommandContext context = mock(CommandContext.class, RETURNS_DEEP_STUBS);
        RoleAssignment assignment = mock(RoleAssignment.class, RETURNS_DEEP_STUBS);
        when(assignment.getRole().permissions()).thenReturn(EnumSet.of(Permission.ViewUnpublishedDataset));
        when(assignment.getAssigneeIdentifier()).thenReturn(recipient.getIdentifier());
        when(context.roles().rolesAssignments(dataset)).thenReturn(Set.of(assignment));
        when(context.roleAssignees().getRoleAssignee(recipient.getIdentifier())).thenReturn(recipient);
        when(context.roleAssignees().getExplicitUsers(recipient)).thenReturn(List.of(recipient));
        when(context.workflows().getDefaultWorkflow(any())).thenReturn(Optional.empty());

        assertTrue(new FinalizeDatasetPublicationCommand(dataset, request).onSuccess(context, dataset));

        verify(context.notifications()).sendNotificationInNewTransaction(eq(recipient), any(Timestamp.class),
                eq(UserNotification.Type.PUBLISHEDDS), eq(42L), eq(locale));
    }
}
