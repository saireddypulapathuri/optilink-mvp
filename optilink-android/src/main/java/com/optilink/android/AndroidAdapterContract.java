package com.optilink.android;

/**
 * Android adapter contract marker for the Kotlin mobile implementation.
 */
public final class AndroidAdapterContract {

    private AndroidAdapterContract() {
    }

    /**
     * Returns the adapter name for diagnostics.
     *
     * @return adapter name
     */
    public static String adapterName() {
        return "optilink-android";
    }
}
