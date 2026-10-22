package edu.harvard.iq.dataverse.util;

import edu.harvard.iq.dataverse.DataFile;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.UserNotification;
import edu.harvard.iq.dataverse.branding.BrandingUtil;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class MailUtil {

    private static final Logger logger = Logger.getLogger(MailUtil.class.getCanonicalName());

    public static String getSubjectTextBasedOnNotification(UserNotification userNotification, Object objectOfNotification) {
        return getSubjectTextBasedOnNotification(userNotification, objectOfNotification, BundleUtil.getResourceBundle("Bundle"));
    }

    public static String getSubjectTextBasedOnNotification(UserNotification userNotification,
            Object objectOfNotification, ResourceBundle bundle) {
        List<String> rootDvNameAsList = Arrays.asList(BrandingUtil.getInstallationBrandName());
        String datasetDisplayName = "";

        if (objectOfNotification != null) {
            if (objectOfNotification instanceof Dataset) {
                datasetDisplayName = ((Dataset) objectOfNotification).getDisplayName();
            } else if (objectOfNotification instanceof DatasetVersion) {
                datasetDisplayName = ((DatasetVersion) objectOfNotification).getDataset().getDisplayName();
            } else if (objectOfNotification instanceof DataFile) {
                datasetDisplayName = ((DataFile) objectOfNotification).getOwner().getDisplayName();
            }
        }

        switch (userNotification.getType()) {
            case ASSIGNROLE:
                return BundleUtil.getStringFromBundle("notification.email.assign.role.subject", rootDvNameAsList, bundle);
            case REVOKEROLE:
                return BundleUtil.getStringFromBundle("notification.email.revoke.role.subject", rootDvNameAsList, bundle);
            case CREATEDV:
                return BundleUtil.getStringFromBundle("notification.email.create.dataverse.subject", rootDvNameAsList, bundle);
            case REQUESTFILEACCESS:
                String userNameFirst = userNotification.getRequestor().getFirstName();
                String userNameLast = userNotification.getRequestor().getLastName();
                String userIdentifier = userNotification.getRequestor().getIdentifier();
                return BundleUtil.getStringFromBundle("notification.email.request.file.access.subject", Arrays.asList(rootDvNameAsList.get(0), userNameFirst, userNameLast, userIdentifier, datasetDisplayName), bundle);
            case REQUESTEDFILEACCESS:
                return BundleUtil.getStringFromBundle("notification.email.requested.file.access.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case GRANTFILEACCESS:
                return BundleUtil.getStringFromBundle("notification.email.grant.file.access.subject", rootDvNameAsList, bundle);
            case REJECTFILEACCESS:
                return BundleUtil.getStringFromBundle("notification.email.rejected.file.access.subject", rootDvNameAsList, bundle);
            case DATASETCREATED:
                return BundleUtil.getStringFromBundle("notification.email.dataset.created.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case DATASETMOVED:
                return BundleUtil.getStringFromBundle("notification.email.dataset.moved.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case CREATEDS:
                return BundleUtil.getStringFromBundle("notification.email.create.dataset.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case SUBMITTEDDS:
                return BundleUtil.getStringFromBundle("notification.email.submit.dataset.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case PUBLISHEDDS:
                return BundleUtil.getStringFromBundle("notification.email.publish.dataset.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case PUBLISHFAILED_PIDREG:
                return BundleUtil.getStringFromBundle("notification.email.publishFailure.dataset.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case RETURNEDDS:
                return BundleUtil.getStringFromBundle("notification.email.returned.dataset.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case WORKFLOW_SUCCESS:
                return BundleUtil.getStringFromBundle("notification.email.workflow.success.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case WORKFLOW_FAILURE:
                return BundleUtil.getStringFromBundle("notification.email.workflow.failure.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case STATUSUPDATED:
                return BundleUtil.getStringFromBundle("notification.email.status.change.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case PIDRECONCILED:
                return BundleUtil.getStringFromBundle("notification.email.pid.reconciled.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case CREATEACC:
                return BundleUtil.getStringFromBundle("notification.email.create.account.subject", rootDvNameAsList, bundle);
            case CHECKSUMFAIL:
                return BundleUtil.getStringFromBundle("notification.email.checksumfail.subject", rootDvNameAsList, bundle);
            case FILESYSTEMIMPORT:
                try {
                    DatasetVersion version =  (DatasetVersion)objectOfNotification;
                    List<String> dsNameAsList = Arrays.asList(version.getDataset().getDisplayName());
                    return BundleUtil.getStringFromBundle("notification.email.import.filesystem.subject", dsNameAsList, bundle);
                } catch (Exception e) {
                    return BundleUtil.getStringFromBundle("notification.email.import.filesystem.subject", rootDvNameAsList, bundle);
                }
            case GLOBUSUPLOADCOMPLETED:
                try {
                    DatasetVersion version =  (DatasetVersion)objectOfNotification;
                    List<String> dsNameAsList = Arrays.asList(version.getDataset().getDisplayName());
                    return BundleUtil.getStringFromBundle("notification.email.globus.uploadCompleted.subject", dsNameAsList, bundle);
                } catch (Exception e) {
                    return BundleUtil.getStringFromBundle("notification.email.globus.uploadCompleted.subject", rootDvNameAsList, bundle);
                }
            case GLOBUSDOWNLOADCOMPLETED:
                try {
                    DatasetVersion version =  (DatasetVersion)objectOfNotification;
                    List<String> dsNameAsList = Arrays.asList(version.getDataset().getDisplayName());
                    return BundleUtil.getStringFromBundle("notification.email.globus.downloadCompleted.subject", dsNameAsList, bundle);
                } catch (Exception e) {
                    return BundleUtil.getStringFromBundle("notification.email.globus.downloadCompleted.subject", rootDvNameAsList, bundle);
                }
            case GLOBUSUPLOADCOMPLETEDWITHERRORS:
                try {
                    DatasetVersion version =  (DatasetVersion)objectOfNotification;
                    List<String> dsNameAsList = Arrays.asList(version.getDataset().getDisplayName());
                    return BundleUtil.getStringFromBundle("notification.email.globus.uploadCompletedWithErrors.subject", dsNameAsList, bundle);
                } catch (Exception e) {
                    return BundleUtil.getStringFromBundle("notification.email.globus.uploadCompletedWithErrors.subject", rootDvNameAsList, bundle);
                }
            case GLOBUSUPLOADREMOTEFAILURE:
                try {
                    DatasetVersion version =  (DatasetVersion)objectOfNotification;
                    List<String> dsNameAsList = Arrays.asList(version.getDataset().getDisplayName());
                    return BundleUtil.getStringFromBundle("notification.email.globus.uploadFailedRemotely.subject", dsNameAsList, bundle);
                    
                } catch (Exception e) {
                    return BundleUtil.getStringFromBundle("notification.email.globus.uploadFailedRemotely.subject", rootDvNameAsList, bundle);
                }
            case GLOBUSUPLOADLOCALFAILURE:
                try {
                    DatasetVersion version =  (DatasetVersion)objectOfNotification;
                    List<String> dsNameAsList = Arrays.asList(version.getDataset().getDisplayName());
                    return BundleUtil.getStringFromBundle("notification.email.globus.uploadFailedLocally.subject", dsNameAsList, bundle);
                } catch (Exception e) {
                    return BundleUtil.getStringFromBundle("notification.email.globus.uploadFailedLocally.subject", rootDvNameAsList, bundle);
                }
            case GLOBUSDOWNLOADCOMPLETEDWITHERRORS:
                try {
                    DatasetVersion version =  (DatasetVersion)objectOfNotification;
                    List<String> dsNameAsList = Arrays.asList(version.getDataset().getDisplayName());
                    return BundleUtil.getStringFromBundle("notification.email.globus.downloadCompletedWithErrors.subject", dsNameAsList, bundle);
                } catch (Exception e) {
                    return BundleUtil.getStringFromBundle("notification.email.globus.downloadCompletedWithErrors.subject", rootDvNameAsList, bundle);
                }

            case CHECKSUMIMPORT:
                return BundleUtil.getStringFromBundle("notification.email.import.checksum.subject", rootDvNameAsList, bundle);
            case CONFIRMEMAIL:
                return BundleUtil.getStringFromBundle("notification.email.verifyEmail.subject", rootDvNameAsList, bundle);
            case APIGENERATED:
                return BundleUtil.getStringFromBundle("notification.email.apiTokenGenerated.subject",  rootDvNameAsList, bundle);
            case INGESTCOMPLETED:
                return BundleUtil.getStringFromBundle("notification.email.ingestCompleted.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case INGESTCOMPLETEDWITHERRORS:
                return BundleUtil.getStringFromBundle("notification.email.ingestCompletedWithErrors.subject", Arrays.asList(rootDvNameAsList.get(0), datasetDisplayName), bundle);
            case DATASETMENTIONED:
                return BundleUtil.getStringFromBundle("notification.email.datasetWasMentioned.subject", rootDvNameAsList, bundle);
        }
        return "";
    }

}
