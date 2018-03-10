package sugar.free.sightremote.utils;

import android.widget.NumberPicker;

import lombok.Setter;

public class BolusAmountPicker implements NumberPicker.OnValueChangeListener {

    private double maxValue;
    private int digit1Max;
    private int digit2Max;

    private NumberPicker digit1;
    private NumberPicker digit2;
    private NumberPicker digit3;
    private NumberPicker digit4;

    @Setter
    private OnAmountChangeListener onAmountChangeListener;

    public BolusAmountPicker(NumberPicker digit1, NumberPicker digit2, NumberPicker digit3, NumberPicker digit4) {
        this.digit1 = digit1;
        this.digit2 = digit2;
        this.digit3 = digit3;
        this.digit4 = digit4;
        digit1.setOnValueChangedListener(this);
        digit2.setOnValueChangedListener(this);
        digit3.setOnValueChangedListener(this);
        digit4.setOnValueChangedListener(this);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (picker == digit1) {
            if (getPickerValue() > maxValue) {
                digit2.setValue(digit2Max);
                digit3.setValue(0);
                digit4.setValue(0);
            }
        } else if (picker == digit2) {
            if (getPickerValue() > maxValue) {
                if (getPickerValue() - ((double) digit3.getValue()) / 10D - ((double) digit4.getValue()) / 100D <= maxValue) {
                    digit3.setValue(0);
                    digit4.setValue(0);
                } else digit1.setValue(digit1.getValue() - 1);
            }
        } else if (picker == digit3) {
            if (getPickerValue() > maxValue) {
                if (getPickerValue() - ((double) digit4.getValue()) / 100D <= maxValue) {
                    digit4.setValue(0);
                } else {
                    if (digit2.getValue() != 0) digit2.setValue(digit2.getValue() - 1);
                    else digit1.setValue(digit1.getValue() - 1);
                }
            }
        } else if (picker == digit4) {
            if (getPickerValue() > maxValue) {
                if (digit3.getValue() != 0) digit1.setValue(digit1.getValue() - 1);
                else if (digit2.getValue() != 0) digit2.setValue(digit2.getValue() - 1);
                else digit1.setValue(digit2.getValue() - 1);
            }
        }
        if (onAmountChangeListener != null) onAmountChangeListener.onAmountChange(this, getPickerValue());
    }

    public void setValue(double value) {
        int digit1Value = (int) (value / 10D);
        value -= (double) digit1Value * 10D;
        int digit2Value = (int) value;
        value -= (double) digit2Value;
        int digit3Value = (int) (value * 10D);
        int digit4Value = (int) (value * 100D - ((double) digit3Value) * 10D);
        digit1.setValue(digit1Value);
        digit2.setValue(digit2Value);
        digit3.setValue(digit3Value);
        digit4.setValue(digit4Value);
    }

    public double getPickerValue() {
        double value = 0;
        value += digit1.getValue() * 10;
        value += digit2.getValue();
        value += ((double) digit3.getValue()) / 10D;
        value += ((double) digit4.getValue()) / 100D;
        return value;
    }

    public void adjustNumberPickers(double max) {
        this.maxValue = max;
        digit1Max = ((int) max) / 10;
        digit2Max = ((int) max) - digit1Max * 10;
        digit1.setMinValue(0);
        digit2.setMinValue(0);
        digit3.setMinValue(0);
        digit4.setMinValue(0);
        digit1.setMaxValue(digit1Max);
        digit2.setMaxValue(9);
        digit3.setMaxValue(9);
        digit4.setMaxValue(9);
    }

    public interface OnAmountChangeListener {
        void onAmountChange(BolusAmountPicker bolusAmountPicker, double newValue);
    }
}
