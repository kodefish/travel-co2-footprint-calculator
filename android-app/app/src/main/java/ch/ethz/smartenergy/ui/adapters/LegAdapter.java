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
import ch.ethz.smartenergy.footprint.TripType;
import ch.ethz.smartenergy.ui.util.OnItemClickListener;

public class LegAdapter extends ArrayAdapter {

    private final List<Leg> legs;
    private final OnItemClickListener onItemClickListener;

    public LegAdapter(@NonNull Context context, int resource, List<Leg> legs, OnItemClickListener onItemClickListener) {
        super(context, resource);
        this.legs = legs;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leg, parent, false);

        ImageView legIcon = v.findViewById(R.id.item_leg_icon_type);
        TextView legType = v.findViewById(R.id.item_leg_type);
        TextView legDistance = v.findViewById(R.id.item_leg_length);
        TextView legEmissions = v.findViewById(R.id.item_leg_emissions);

        Leg leg = legs.get(position);

        TripType type = leg.getMostProbableLegType();
        Picasso.get().load(TripType.getTripTypeIconResource(type)).into(legIcon);

        legType.setText(leg.getMostProbableLegType().toString());
        legDistance.setText(leg.getLegDistanceAsString());
        legEmissions.setText(leg.getLegFootprintAsString());

        // React to click since bottom sheet does something funky
        v.setOnClickListener(v1 -> onItemClickListener.onItemClickListener(position));
        return v;
    }

    @Override
    public int getCount() {
        return legs.size();
    }
}
