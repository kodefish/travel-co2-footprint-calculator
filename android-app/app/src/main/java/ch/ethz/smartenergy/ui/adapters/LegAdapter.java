package ch.ethz.smartenergy.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.List;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.Leg;

public class LegAdapter extends ArrayAdapter {

    private final List<Leg> legs;

    public LegAdapter(@NonNull Context context, int resource, List<Leg> legs) {
        super(context, resource);
        this.legs = legs;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leg, parent, false);

        ImageView legIcon = v.findViewById(R.id.item_leg_icon_type);
        TextView legType = v.findViewById(R.id.item_leg_trip_title_type);
        TextView legDistance = v.findViewById(R.id.item_leg_distance_covered);
        TextView legEmissions = v.findViewById(R.id.item_leg_emissions);

        Leg leg = legs.get(position);

        int iconResourceId = R.drawable.icon_home;
        Picasso.get().load(iconResourceId).into(legIcon);

        legType.setText(leg.getMostProbableLegType().toString());
        legDistance.setText(leg.getLegLengthAsString());
        legEmissions.setText(leg.getLegEmissionsAsString());

        return v;
    }

    @Override
    public int getCount() {
        return legs.size();
    }
}
