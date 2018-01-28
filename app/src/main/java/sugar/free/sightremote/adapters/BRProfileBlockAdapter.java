package sugar.free.sightremote.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import lombok.Setter;
import sugar.free.sightremote.R;
import sugar.free.sightremote.utils.FixedSizeProfileBlock;
import sugar.free.sightremote.utils.UnitFormatter;

public class BRProfileBlockAdapter extends RecyclerView.Adapter<BRProfileBlockAdapter.ViewHolder> {

    @Setter
    private List<FixedSizeProfileBlock> profileBlocks;
    @Setter
    private BRProfileAdapter.OnClickListener onClickListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_edit_br_block, parent, false);
        return new BRProfileBlockAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.blockTime.setText(holder.blockTime.getResources().getString(R.string.br_block_formatter,
                UnitFormatter.formatDuration(profileBlocks.get(position).getStartTime()),
                UnitFormatter.formatDuration(profileBlocks.get(position).getEndTime())));
        holder.blockAmount.setText(UnitFormatter.formatBR(profileBlocks.get(position).getAmount()));
        holder.edit.setOnClickListener(view -> {
            if (onClickListener != null) onClickListener.onClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return profileBlocks == null ? 0 : profileBlocks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView blockTime;
        TextView blockAmount;
        ImageButton edit;

        public ViewHolder(View itemView) {
            super(itemView);
            blockTime = itemView.findViewById(R.id.block_time);
            blockAmount = itemView.findViewById(R.id.block_amount);
            edit = itemView.findViewById(R.id.edit);
        }
    }

    public interface onClickListener {
        void onClick(int position);
    }

}
