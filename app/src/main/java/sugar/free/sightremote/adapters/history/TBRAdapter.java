package sugar.free.sightremote.adapters.history;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightremote.R;
import sugar.free.sightremote.database.EndOfTBR;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class TBRAdapter extends HistoryAdapter<TBRAdapter.ViewHolder, EndOfTBR> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_tbr, parent, false);
        return new TBRAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, EndOfTBR entry, int position) {
        holder.amount.setText(HTMLUtil.getHTML(R.string.history_tbr_amount,
                entry.getAmount(), UnitFormatter.formatDuration(entry.getDuration())));
        holder.dateTime.setText(new SimpleDateFormat(holder.dateTime.getResources().getString(R.string.history_date_time_formatter)).format(entry.getDateTime()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTime;
        private TextView amount;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.date_time);
            amount = itemView.findViewById(R.id.amount);
        }
    }

}
