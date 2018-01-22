package sugar.free.sightremote.utils;

import android.widget.NumberPicker;

import lombok.Setter;

public class DurationPicker implements NumberPicker.OnValueChangeListener {

    private NumberPicker digit1;
    private NumberPicker digit2;

    @Setter
    private OnDurationChangeListener onDurationChangeListener;

    public DurationPicker(NumberPicker digit1, NumberPicker digit2) {
        this.digit1 = digit1;
        this.digit2 = digit2;

        digit1.setMinValue(0);
        digit1.setMaxValue(24);
        digit2.setMinValue(0);
        digit2.setMaxValue(3);
        digit2.setDisplayedValues(new String[] {"0", "15", "30", "45"});

        digit1.setOnValueChangedListener(this);
        digit2.setOnValueChangedListener(this);
    }

    public int getPickerValue() {
        int value = digit1.getValue() * 60;
        value += digit2.getValue() * 15;
        return value;
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (onDurationChangeListener != null) onDurationChangeListener.onDurationChange(getPickerValue());
    }


    public interface OnDurationChangeListener {
        void onDurationChange(int newValue);
    }
}