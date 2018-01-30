package sugar.free.sightremote.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.EditText;

import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightremote.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by jamorham on 28/01/2018.
 */

public class ActivationWarningDialogChain {

    private final SightServiceConnector sightServiceConnector;
    private final Context context;
    private SharedPreferences sharedServicePerferences;

    public ActivationWarningDialogChain(Context context, SightServiceConnector sightServiceConnector) {
        this.context = context;
        this.sightServiceConnector = sightServiceConnector;
    }

    public void doActivationWarning() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.safety_warning));
        builder.setMessage(R.string.safety_warning_text1);
        builder.setNeutralButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(context.getString(R.string.connected_to_a_person), (dialog, which) -> {
            dialog.dismiss();
            doPersonWarningDialog();
        });

        builder.setNegativeButton(context.getString(R.string.never_connected_to_a_person), (dialog, which) -> {
            dialog.dismiss();
            areYouSureDialog();
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void doPersonWarningDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.safety_warning));
        builder.setMessage(context.getString(R.string.are_you_a_qualified_medical_professional));
        builder.setNeutralButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(context.getString(R.string.yes_i_am_a_medical_professional), (dialog, which) -> {
            dialog.dismiss();
            areYouSureDialog();
        });

        builder.setNegativeButton(context.getString(R.string.no), (dialog, which) -> dialog.dismiss());

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void areYouSureDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.safety_warning));
        builder.setMessage(context.getString(R.string.are_you_absolutely_sure_activate));
        builder.setNeutralButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            requestPassword();
        });

        builder.setNegativeButton(context.getString(R.string.no), (dialog, which) -> dialog.dismiss());

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void requestPassword() {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(getServicePreferences().getString("PASSWORD", ""));

        new AlertDialog.Builder(context)
                .setView(input)
                .setTitle(R.string.enter_password)
                .setMessage(context.getString(R.string.please_enter_activation_password))
                .setPositiveButton(R.string.okay, (dialog, which) -> sightServiceConnector.setPassword(input.getText().toString().trim()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private SharedPreferences getServicePreferences() {
        if (sharedServicePerferences == null)
            sharedServicePerferences = context.getSharedPreferences("sugar.free.sightremote.services.SIGHTSERVICE", MODE_PRIVATE);
        return sharedServicePerferences;
    }

}
