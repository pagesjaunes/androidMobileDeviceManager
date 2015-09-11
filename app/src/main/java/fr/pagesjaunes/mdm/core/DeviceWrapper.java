package fr.pagesjaunes.mdm.core;

import java.util.List;

public class DeviceWrapper
{
    private List<Device> results;

    public List<Device> getResults() {
        return results;
    }
    public Device getResult() {
        if (results != null)
        {
            return results.get(0);
        }

        return null;
    }
}
