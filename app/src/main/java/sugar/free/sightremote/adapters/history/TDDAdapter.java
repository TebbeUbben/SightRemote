package sugar.free.sightremote.adapters.history;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import sugar.free.sightremote.R;
import sugar.free.sightremote.database.DailyTotal;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

import java.text.SimpleDateFormat;

public class TDDAdapter extends HistoryAdapter<TDDAdapter.ViewHolder, DailyTotal> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_tdd, parent, false);
        return new TDDAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, DailyTotal entry, int position) {
        holder.bolus.setText(HTMLUtil.getHTML(R.string.history_bolus, UnitFormatter.formatUnits(entry.getBolusTotal())));
        holder.basal.setText(HTMLUtil.getHTML(R.string.history_basal, UnitFormatter.formatUnits(entry.getBasalTotal())));
        holder.total.setText(HTMLUtil.getHTML(R.string.history_total, UnitFormatter.formatUnits(entry.getBolusTotal() + entry.getBasalTotal())));
        holder.dateTime.setText(new SimpleDateFormat(holder.dateTime.getResources().getString(R.string.history_date_formatter)).format(entry.getTotalDate()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTime;
        private TextView bolus;
        private TextView basal;
        private TextView total;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.date_time);
            bolus = itemView.findViewById(R.id.bolus_amount);
            basal = itemView.findViewById(R.id.basal_amount);
            total = itemView.findViewById(R.id.total_amount);
        }
    }

}
