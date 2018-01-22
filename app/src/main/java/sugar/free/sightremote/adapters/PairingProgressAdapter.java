package sugar.free.sightremote.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import sugar.free.sightremote.R;

public class PairingProgressAdapter extends RecyclerView.Adapter<PairingProgressAdapter.ViewHolder> {

    private int progress;

    public void setProgress(int progress) {
        this.progress = progress;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_pairing_progress, parent, false);
        return new PairingProgressAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) {
            holder.title.setText(R.string.connect);
            holder.active.setVisibility(progress == 1 ? View.VISIBLE : View.GONE);
            holder.done.setVisibility(progress > 1 ? View.VISIBLE : View.GONE);
        } else if (position == 1) {
            holder.title.setText(R.string.exchange_keys);
            holder.active.setVisibility(progress == 2 ? View.VISIBLE : View.GONE);
            holder.done.setVisibility(progress > 2 ? View.VISIBLE : View.GONE);
        } else if (position == 2) {
            holder.title.setText(R.string.confirm_connection);
            holder.active.setVisibility(progress == 3 ? View.VISIBLE : View.GONE);
            holder.done.setVisibility(progress > 3 ? View.VISIBLE : View.GONE);
        } else if (position == 3) {
            holder.title.setText(R.string.complete_setup);
            holder.active.setVisibility(progress == 4 ? View.VISIBLE : View.GONE);
            holder.done.setVisibility(progress > 4 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView done;
        ProgressBar active;

        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            done = (ImageView) itemView.findViewById(R.id.done);
            active = (ProgressBar) itemView.findViewById(R.id.active);
        }
    }

}
