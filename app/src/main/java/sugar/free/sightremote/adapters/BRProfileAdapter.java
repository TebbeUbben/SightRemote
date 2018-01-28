package sugar.free.sightremote.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import lombok.Setter;
import sugar.free.sightparser.applayer.configuration.blocks.BRProfileBlock;
import sugar.free.sightparser.applayer.configuration.blocks.NameBlock;
import sugar.free.sightremote.R;
import sugar.free.sightremote.utils.UnitFormatter;

public class BRProfileAdapter extends RecyclerView.Adapter<BRProfileAdapter.ViewHolder> {

    @Setter
    private List<BRProfileBlock> profileBlocks;
    @Setter
    private List<NameBlock> nameBlocks;
    @Setter
    private int activeProfile;
    @Setter
    private BRProfileChangeListener listener;
    @Setter
    private OnClickListener onClickListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_br_profile, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (nameBlocks.get(position).getName().equals(""))
            holder.name.setText(holder.name.getResources().getString(R.string.default_br_name, position + 1));
        else holder.name.setText(nameBlocks.get(position).getName());
        holder.totalUnits.setText(UnitFormatter.format(profileBlocks.get(position).getTotalAmount()));
        if (activeProfile == position) holder.activated.setChecked(true);
        else holder.activated.setChecked(false);
        holder.activated.setOnClickListener((view) -> {
            if (listener != null)
                listener.onProfileChange(position);
        });
        holder.itemView.setOnLongClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onClick(position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return profileBlocks == null ? 0 : 5;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView name;
        TextView totalUnits;
        RadioButton activated;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            name = itemView.findViewById(R.id.name);
            totalUnits = itemView.findViewById(R.id.total_units);
            activated = itemView.findViewById(R.id.activated);
        }
    }

    public interface BRProfileChangeListener {
        void onProfileChange(int profile);
    }

    public interface OnClickListener {
        void onClick(int position);
    }
}
