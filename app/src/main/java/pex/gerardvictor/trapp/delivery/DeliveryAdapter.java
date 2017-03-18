package pex.gerardvictor.trapp.delivery;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import pex.gerardvictor.trapp.R;

/**
 * Created by gerard on 18/03/17.
 */

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder> {

    private List<Delivery> deliveries;

    public DeliveryAdapter(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public DeliveryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.delivery_list_row, parent, false);

        return new DeliveryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeliveryViewHolder holder, int position) {
        Delivery delivery = deliveries.get(position);
        holder.companyName.setText(delivery.getCompany());
        holder.deliveryDate.setText(delivery.getDate());
        holder.deliveryState.setText(delivery.getState());
    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    public class DeliveryViewHolder extends RecyclerView.ViewHolder {

        public TextView companyName, deliveryDate, deliveryState;

        public DeliveryViewHolder(View itemView) {
            super(itemView);
            companyName = (TextView) itemView.findViewById(R.id.company);
            deliveryDate = (TextView) itemView.findViewById(R.id.date);
            deliveryState = (TextView) itemView.findViewById(R.id.state);
        }
    }
}
