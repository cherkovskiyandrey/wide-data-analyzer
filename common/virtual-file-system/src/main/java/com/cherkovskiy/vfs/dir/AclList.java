package com.cherkovskiy.vfs.dir;

import java.nio.file.attribute.AclEntry;
import java.util.List;
import java.util.Objects;

class AclList {
    private final List<AclEntry> aclList;

    AclList(List<AclEntry> aclList) {
        this.aclList = aclList;
    }

    public List<AclEntry> getAclList() {
        return aclList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AclList aclList1 = (AclList) o;
        return Objects.equals(aclList, aclList1.aclList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aclList);
    }
}
