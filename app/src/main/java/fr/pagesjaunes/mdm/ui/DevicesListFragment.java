package fr.pagesjaunes.mdm.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import fr.pagesjaunes.mdm.BootstrapServiceProvider;
import fr.pagesjaunes.mdm.Injector;
import fr.pagesjaunes.mdm.R;
import fr.pagesjaunes.mdm.authenticator.LogoutService;
import fr.pagesjaunes.mdm.core.Device;
import fr.pagesjaunes.mdm.util.Ln;
import fr.pagesjaunes.mdm.wishlist.SingleTypeAdapter;

public class DevicesListFragment extends ItemListFragment<Device>
{
	@Inject
	protected BootstrapServiceProvider serviceProvider;
	@Inject
	protected LogoutService logoutService;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Injector.inject(this);
		Ln.d("Here");
	}



	@Override
	protected void configureList(final Activity activity, final ListView listView)
	{
		super.configureList(activity, listView);

		listView.setFastScrollEnabled(true);
		listView.setDividerHeight(0);

		getListAdapter().addHeader(activity.getLayoutInflater().inflate(R.layout.checkins_list_item_labels, null));
	}

	@Override
	protected LogoutService getLogoutService()
	{
		return logoutService;
	}

	@Override
	public void onDestroyView()
	{
		setListAdapter(null);

		super.onDestroyView();
	}

	@Override
	public Loader<List<Device>> onCreateLoader(final int id, final Bundle args)
	{
		final List<Device> initialItems = items;

		return new ThrowableLoader<List<Device>>(getActivity(), items)
		{

			@Override
			public List<Device> loadData() throws Exception
			{
				try
				{
					if (getActivity() != null)
					{
						return serviceProvider.getService(getActivity()).getDevices();
					}
					else
					{
						return Collections.emptyList();
					}
				}
				catch (final OperationCanceledException e)
				{
					final Activity activity = getActivity();
					if (activity != null)
					{
						activity.finish();
					}
					return initialItems;
				}
			}
		};
	}

	@Override
	protected SingleTypeAdapter<Device> createAdapter(final List<Device> items)
	{
		return new DevicesListAdapter(getActivity().getLayoutInflater(), items);
	}

	public void onListItemClick(ListView l, View v, int position, long id)
	{
		Device device = ((Device) l.getItemAtPosition(position));

	}

	@Override
	protected int getErrorMessage(final Exception exception)
	{
		return R.string.error_loading_checkins;
	}



}
