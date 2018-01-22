package sugar.free.sightremote.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sugar.free.sightremote.R;

public class TextAdapter extends RecyclerView.Adapter<TextAdapter.ViewHolder> {

    private List<String> text = new ArrayList<>();

    public List<String> getText() {
        return text;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_text, parent, false);
        return new TextAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text.setText(text.get(position));
    }

    @Override
    public int getItemCount() {
        return text.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }

    }
}
