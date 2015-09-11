package fr.pagesjaunes.mdm.core;

import retrofit.http.GET;
import retrofit.http.Path;

public interface DeviceService
{

    @GET(Constants.Http.URL_DEVICE_FRAG)

    //'where={"applicationId":{"__type":"Pointer","className":"ApplicationVersion","objectId":"{id}"}}'
    DeviceWrapper getDevices();

    @GET(Constants.Http.URL_DEVICE_FRAG+"/{id}")

    DeviceWrapper findDeviceById(@Path("id") String id);
}
