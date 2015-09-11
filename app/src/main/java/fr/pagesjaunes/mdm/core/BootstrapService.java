package fr.pagesjaunes.mdm.core;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import retrofit.RestAdapter;

/**
 * Bootstrap API service
 */
public class BootstrapService
{

	private RestAdapter restAdapter;

	/**
	 * Create bootstrap service Default CTOR
	 */
	public BootstrapService()
	{
	}

	/**
	 * Create bootstrap service
	 *
	 * @param restAdapter The RestAdapter that allows HTTP Communication.
	 */
	public BootstrapService(RestAdapter restAdapter)
	{
		this.restAdapter = restAdapter;
	}

	private DeviceService getDeviceService()
	{
		return getRestAdapter().create(DeviceService.class);
	}

	private RestAdapter getRestAdapter()
	{
		return restAdapter;
	}

	/**
	 * Get all bootstrap Checkins that exists on Parse.com
	 */
	public List<Device> getDevices()
	{
		//return getDeviceService().getDevices().getResults();
		ParseQuery<Device> query = ParseQuery.getQuery("Device");
		try
		{
			return query.find();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public Device findDeviceById(String id)
	{
		return getDeviceService().findDeviceById(id).getResult();
	}


	public void authenticate(String email, String password)
	{
		ParseUser.logInInBackground(email, password, new LogInCallback()
		{
			public void done(ParseUser user, ParseException e)
			{
				if (e == null && user != null)
				{
				}
				else if (user == null)
				{
					//  usernameOrPasswordIsInvalid();
				}
				else
				{
					//  somethingWentWrong();
				}
			}
		});
	}
}