package com.cherkovskiy.vfs;

import javax.annotation.Nullable;

public interface Attributes {

    /**
     * Unix mode.
     * null - means is unsupported or could not be accessed
     *
     * @return
     */
    Integer getUnixMode();

    /**
     * User.
     * null - means is unsupported or could not be accessed
     *
     * @return
     */
    @Nullable
    String getOwner();

    /**
     * Group.
     * null - means is unsupported or could not be accessed
     *
     * @return
     */
    @Nullable
    String getGroup();

    /**
     * Return extra attributes by token if support.
     * null if token is not supported.
     *
     * @param token
     * @param <T>
     * @return
     */
    <T> T getExtraAttributesAs(Class<T> token);

    @Override
    String toString();

}
