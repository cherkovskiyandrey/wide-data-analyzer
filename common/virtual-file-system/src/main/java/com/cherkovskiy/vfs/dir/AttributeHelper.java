package com.cherkovskiy.vfs.dir;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

class AttributeHelper {
    private static final Logger LOG = LoggerFactory.getLogger(AttributeHelper.class);

    static void setAttributes(File file, Attributes attributes) {
        final Attributes curAttributes = getAttributes(file);
        trySetOwner(file, curAttributes.getOwner(), attributes.getOwner());

        if (SystemUtils.IS_OS_LINUX) {
            trySetGroup(file, curAttributes.getGroup(), attributes.getGroup());
            trySetUnixPermissions(file, curAttributes.getUnixMode(), attributes.getUnixMode());
        } else if (SystemUtils.IS_OS_WINDOWS) {
            trySetWinACL(file, curAttributes.getExtraAttributesAs(AclList.class), attributes.getExtraAttributesAs(AclList.class));
        }
    }


    static Attributes getAttributes(File file) {
        final String owner = tryGetOwner(file);
        if (SystemUtils.IS_OS_LINUX) {
            final String group = tryGetGroup(file);
            final Set<PosixFilePermission> perm = tryGetUnixPermissions(file);
            return new BaseAttributesImpl((perm != null ? unixModeAsInt(perm) : null), owner, group);

        } else if (SystemUtils.IS_OS_WINDOWS) {
            final List<AclEntry> perm = tryGetWinAcl(file);
            return new BaseAttributesImpl(null, owner, null, perm != null ? new AclList(perm) : null);
        }

        return new BaseAttributesImpl(null, owner, null);
    }

    private static List<AclEntry> tryGetWinAcl(File file) {
        final AclFileAttributeView aclView = Files.getFileAttributeView(file.toPath(), AclFileAttributeView.class);
        if (aclView == null) {
            LOG.warn("Could not get ACL attributes for: " + file.getAbsolutePath());
            return null;
        }
        try {
            return aclView.getAcl();
        } catch (IOException e) {
            LOG.warn("Could not get ACL attributes for: " + file.getAbsolutePath());
            return null;
        }
    }

    private static Set<PosixFilePermission> tryGetUnixPermissions(File file) {
        try {
            return Files.readAttributes(file.toPath(), PosixFileAttributes.class).permissions();
        } catch (IOException e) {
            LOG.warn("Could not get POSIX permissions for: " + file.getAbsolutePath());
            return null;
        }
    }

    private static String tryGetGroup(File file) {
        try {
            return Files.readAttributes(file.toPath(), PosixFileAttributes.class).group().getName();
        } catch (IOException e) {
            LOG.warn("Could not get POSIX group name for: " + file.getAbsolutePath());
            return null;
        }
    }

    private static String tryGetOwner(File file) {
        try {
            return Files.getOwner(file.toPath(), NOFOLLOW_LINKS).getName();
        } catch (IOException e) {
            LOG.warn("Could not get owner for: " + file.getAbsolutePath());
            return null;
        }
    }

    private static void trySetOwner(File file, String curOwner, String desiredOwner) {
        if (StringUtils.isNotBlank(desiredOwner) && !desiredOwner.equals(curOwner)) {
            try {
                final UserPrincipal userPrincipal = file.toPath().getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(desiredOwner);
                Files.setOwner(file.toPath(), userPrincipal);
            } catch (IOException e) {
                LOG.warn("Could not set owner for: " + file.getAbsolutePath());
            }
        }
    }


    private static void trySetGroup(File file, String curGroup, String desiredGroup) {
        if (StringUtils.isNotBlank(desiredGroup) && !desiredGroup.equals(curGroup)) {
            try {
                final GroupPrincipal newGroup = file.toPath().getFileSystem().getUserPrincipalLookupService().lookupPrincipalByGroupName(desiredGroup);
                Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class).setGroup(newGroup);
            } catch (IOException e) {
                LOG.warn("Could not set POSIX group name for: " + file.getAbsolutePath());
            }
        }
    }


    private static void trySetUnixPermissions(File file, Integer curMode, Integer desiredMode) {
        if (desiredMode != null && !desiredMode.equals(curMode)) {
            try {
                Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class).setPermissions(unixModeAsSet(desiredMode));
            } catch (IOException e) {
                LOG.warn("Could not set POSIX permissions for: " + file.getAbsolutePath());
            }
        }
    }


    private static void trySetWinACL(File file, AclList curAclList, AclList desiredAclList) {
        if (desiredAclList != null && !desiredAclList.equals(curAclList)) {
            try {
                final AclFileAttributeView aclView = Files.getFileAttributeView(file.toPath(), AclFileAttributeView.class);
                aclView.setAcl(desiredAclList.getAclList());
            } catch (IOException e) {
                LOG.warn("Could not set ACL attributes for: " + file.getAbsolutePath());
            }
        }
    }

    private static Set<PosixFilePermission> unixModeAsSet(int unixMode) {
        final Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);
        for (int i = 0; i < 9; i++) {
            if (((unixMode >>> i) & 1) != 0) {
                result.add(PosixFilePermission.values()[8 - i]);
            }
        }

        return result;
    }

    private static int unixModeAsInt(Set<PosixFilePermission> var) {
        int result = 0;
        for (PosixFilePermission posixFilePermission : var) {
            result |= (1 << (8 - posixFilePermission.ordinal()));
        }
        return result;
    }
}
