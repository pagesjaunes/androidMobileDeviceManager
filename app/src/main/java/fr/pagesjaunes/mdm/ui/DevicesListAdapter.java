package fr.pagesjaunes.mdm.ui;

import android.view.LayoutInflater;

import java.util.List;

import fr.pagesjaunes.mdm.R;
import fr.pagesjaunes.mdm.core.Device;

public class DevicesListAdapter extends AlternatingColorListAdapter<Device> {
    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public DevicesListAdapter(final LayoutInflater inflater, final List<Device> items, final boolean selectable) {
        super(R.layout.checkin_list_item, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public DevicesListAdapter(final LayoutInflater inflater, final List<Device> items) {
        super(R.layout.checkin_list_item, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.tv_name, R.id.tv_user,
                R.id.tv_date};
    }

    @Override
    protected void update(final int position, final Device item) {
        super.update(position, item);

        setText(0, item.getDevice());
        setText(1, item.computeUserName());
        setText(2, item.printUpdatedAt());
    }
}
