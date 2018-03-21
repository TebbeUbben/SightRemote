package sugar.free.sightremote.adapters.history;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;
import sugar.free.sightremote.R;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

import java.text.SimpleDateFormat;
import java.util.List;

public class BolusAdapter extends HistoryAdapter<BolusAdapter.ViewHolder, BolusDelivered> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new BolusAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, BolusDelivered entry, int position) {
        HistoryBolusType bolusType = entry.getBolusType();
        if (bolusType == HistoryBolusType.STANDARD || bolusType == HistoryBolusType.MULTIWAVE) {
            holder.immediateAmount.setText(HTMLUtil.getHTML(R.string.history_immediate_amount,
                    UnitFormatter.formatUnits(entry.getImmediateAmount())));
        }
        if (bolusType == HistoryBolusType.EXTENDED || bolusType == HistoryBolusType.MULTIWAVE) {
            holder.extendedAmount.setText(HTMLUtil.getHTML(R.string.history_extended_amount,
                    UnitFormatter.formatUnits(entry.getImmediateAmount()), UnitFormatter.formatDuration(entry.getDuration())));
        }
        holder.dateTime.setText(new SimpleDateFormat(holder.dateTime.getResources().getString(R.string.history_date_time_formatter)).format(entry.getDateTime()));
    }

    @Override
    public int getItemViewType(BolusDelivered entry, int position) {
        if (entry.getBolusType() == HistoryBolusType.STANDARD) return R.layout.adapter_bolus_standard;
        else if (entry.getBolusType() == HistoryBolusType.EXTENDED) return R.layout.adapter_bolus_extended;
        else if (entry.getBolusType() == HistoryBolusType.MULTIWAVE) return R.layout.adapter_bolus_multiwave;
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTime;
        private TextView immediateAmount;
        private TextView extendedAmount;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.date_time);
            immediateAmount = itemView.findViewById(R.id.immediate_amount);
            extendedAmount = itemView.findViewById(R.id.extended_amount);
        }
    }

}
