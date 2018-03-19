package sugar.free.sightremote.adapters;

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
import sugar.free.sightremote.database.DailyTotal;
import sugar.free.sightremote.database.EndOfTBR;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class TDDAdapter extends RecyclerView.Adapter<TDDAdapter.ViewHolder> {

    @Getter
    @Setter
    private List<DailyTotal> dailyTotals;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_tbr, parent, false);
        return new TDDAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DailyTotal dailyTotal = dailyTotals.get(position);
        holder.bolus.setText(HTMLUtil.getHTML(R.string.history_bolus, UnitFormatter.formatUnits(dailyTotal.getBolusTotal())));
        holder.basal.setText(HTMLUtil.getHTML(R.string.history_basal, UnitFormatter.formatUnits(dailyTotal.getBasalTotal())));
        holder.total.setText(HTMLUtil.getHTML(R.string.history_total, UnitFormatter.formatUnits(dailyTotal.getBolusTotal() + dailyTotal.getBasalTotal())));
        holder.dateTime.setText(new SimpleDateFormat(holder.dateTime.getResources().getString(R.string.history_date_time_formatter)).format(dailyTotal.getDateTime()));
    }

    @Override
    public int getItemCount() {
        return dailyTotals == null ? 0 : dailyTotals.size();
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
