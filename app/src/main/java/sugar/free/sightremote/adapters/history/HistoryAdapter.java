package sugar.free.sightremote.adapters.history;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public abstract class HistoryAdapter<VH extends RecyclerView.ViewHolder, T> extends RecyclerView.Adapter<VH> {

    @Getter
    @Setter
    private List<T> historyEntries;

    public HistoryAdapter() {

    }

    @Override
    public int getItemCount() {
        return historyEntries == null ? 0 : historyEntries.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        onBindViewHolder(holder, historyEntries.get(position), position);
    }

    public abstract void onBindViewHolder(@NonNull VH holder, T entry, int position);

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(historyEntries.get(position), position);
    }

    public int getItemViewType(T entry, int position) {
        return 0;
    }
}
