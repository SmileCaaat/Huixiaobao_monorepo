package com.ruoyi.fire.service;

import java.util.Map;

/**
 * Server-side reverse geocoding. Mini program must not call map vendors directly.
 */
public interface IGeoCodingService
{
    /**
     * Convert GCJ-02 coordinates to a Chinese address payload.
     *
     * @return data with longitude/latitude/address
     */
    Map<String, Object> reverseGeocode(Double longitude, Double latitude);

    /**
     * Resolve Chinese address text; throws ServiceException on failure.
     */
    String resolveChineseAddress(Double longitude, Double latitude);
}
