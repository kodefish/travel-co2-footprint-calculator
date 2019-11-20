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

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.TripType;

public class PredictionAdapter extends ArrayAdapter {

    private float[] predictions = {};

    public PredictionAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void setPredictions(float[] predictions) {
        this.predictions = predictions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prediction, parent, false);

        ImageView predictionIcon = v.findViewById(R.id.item_prediction_icon_mode);
        TextView predictionCertainty = v.findViewById(R.id.item_prediction_certainty);

        int predictionIconResource = getIconResource(position);
        Picasso.get().load(predictionIconResource).into(predictionIcon);

        predictionCertainty.setText(Float.toString(predictions[position] * 100));

        return v;
    }

    /**
     * Get icon resource based on trip type
     * @param type of trip
     * @return resource id of icon trip
     */
    private int getIconResource(int type) {
        int iconResource = 0;
        switch (TripType.values()[type]) {
            case FOOT:
                iconResource = R.drawable.icon_foot; break;
            case TRAIN:
                iconResource = R.drawable.icon_train; break;
            case BUS:
                iconResource = R.drawable.icon_bus; break;
            case CAR:
                iconResource = R.drawable.icon_car; break;
            case TRAM:
                iconResource = R.drawable.icon_tram; break;
            case BIKE:
                iconResource = R.drawable.icon_bike; break;
            case EBIKE:
                iconResource = R.drawable.icon_ebike; break;
            case MOTORCYCLE:
                iconResource = R.drawable.icon_motorcycle; break;
        }
        return iconResource;
    }

    @Override
    public int getCount() {
        return predictions.length;
    }
}
