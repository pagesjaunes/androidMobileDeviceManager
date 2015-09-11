package fr.pagesjaunes.mdm.core;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import fr.pagesjaunes.mdm.util.Strings;

@ParseClassName("Device")
public class Device extends ParseObject implements Serializable {


    public String getManufacturer()
    {
        return getString("manufacturer");
	}

    public String getModel()
    {
        return getString("model");
    }

    public String getOsVersion()
    {
         return getString("osVersion");
    }

    public String getType()
    {
		return getString("type");
    }


    public String computeUserName()
    {
		ParseUser user = getParseUser("user");


		if (user != null)
		{
			try
			{
				user.fetchIfNeeded();
				return user.getUsername();
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}

		}

		return "N/A";
    }
    public String getDevice()
    {
        return String.format("%s %s (%s)", getManufacturer(), getModel(), getOsVersion());
    }

    public void setManufacturer(String manufacturer)
    {
        put("manufacturer", manufacturer);
    }

    public void setModel(String model)
    {
		put("model", model);
    }

    public void setOsVersion(String osVersion)
    {
		put("osVersion", osVersion);
    }

    public void setType(String type)
    {
		put("type", type);
    }

	public void setUser(ParseUser user)
	{
		if (user != null)
		{
			put("user", user);
		}
	}

	public String printUpdatedAt()
	{
		Date today = new Date();
		Date updatedAt = getUpdatedAt();
		if (updatedAt != null)
		{
			Long lDiff = (today.getTime() - updatedAt.getTime()) / 1000;
			double diff = Math.abs(Math.floor(lDiff));

			double days = Math.floor(diff / (24 * 60 * 60));
			double leftSec = diff - days * 24 * 60 * 60;

			double hrs = Math.floor(leftSec / (60 * 60));
			leftSec = leftSec - hrs * 60 * 60;

			double min = Math.floor(leftSec / (60));
			leftSec = leftSec - min * 60;

			HashMap<String, Double> result = new HashMap<String, Double>();
			result.put("days", days);
			result.put("hours", hrs);
			result.put("minutes", min);
			return Strings.prettifyDiffRelativeDate(result);
		}
		return "n/a";
	}

}

