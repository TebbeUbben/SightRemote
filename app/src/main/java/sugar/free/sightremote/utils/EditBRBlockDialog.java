package sugar.free.sightremote.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import sugar.free.sightremote.R;

public class EditBRBlockDialog {

    public static void showDialog(Context context, BlockChangedListener listener,
                                  FixedSizeProfileBlock profileBlock, boolean disableEnd, int minEndTime, double min, double max) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_br_block, null);
        EditText amount = view.findViewById(R.id.br_amount);
        amount.setText(profileBlock.getAmount() + "");
        Spinner endTime = view.findViewById(R.id.br_end_time);
        endTime.setAdapter(getEndTimeSpinnerAdapter(context, minEndTime));
        endTime.setSelection((profileBlock.getEndTime() - minEndTime) / 15);
        endTime.setEnabled(!disableEnd);
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setTitle(R.string.edit_block)
                .setPositiveButton(R.string.okay, ((dialog, which) -> {
                    listener.onBlockChange(profileBlock,
                            endTime.getSelectedItemPosition() * 15 + minEndTime,
                            Double.parseDouble(amount.getText().toString()));
                })).setNegativeButton(R.string.cancel, null)
                .create();
        alertDialog.show();
        Button okayButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.equals("")) {
                    amount.setError(context.getString(R.string.empty));
                    okayButton.setEnabled(false);
                    return;
                }
                double value = Double.parseDouble(text);
                String[] split = text.split("\\.");
                if (split.length == 2 && split[1].length() > 2) {
                    amount.setError(context.getString(R.string.maximum_of_two_decimal_places));
                    okayButton.setEnabled(false);
                } else if (value < min) {
                    amount.setError(context.getString(R.string.minimum_of, UnitFormatter.formatBR(min)));
                    okayButton.setEnabled(false);
                } else if (value > max) {
                    amount.setError(context.getString(R.string.maximum_of, UnitFormatter.formatBR(max)));
                    okayButton.setEnabled(false);
                } else {
                    amount.setError(null);
                    okayButton.setEnabled(true);
                }
            }
        });
    }

    private static ArrayAdapter<String> getEndTimeSpinnerAdapter(Context context, int minEndTime) {
        List<String> values = new ArrayList<>();
        for (int i = minEndTime; i <= 24 * 60; i += 15)
            values.add(UnitFormatter.formatDuration(i));
        return new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, values);
    }

    public interface BlockChangedListener {
        void onBlockChange(FixedSizeProfileBlock profileBlock, int endTime, double amount);
    }

}
