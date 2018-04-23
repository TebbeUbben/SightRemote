package sugar.free.sightremote.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;
import java.util.List;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.ConfigurationBlock;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.ActiveProfileBlock;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRName1Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRName2Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRName3Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRName4Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRName5Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile1Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile2Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile3Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile4Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile5Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfileBlock;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.NameBlock;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.handling.taskrunners.ReadConfigurationTaskRunner;
import sugar.free.sightparser.handling.taskrunners.WriteConfigurationTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.BRProfileAdapter;
import sugar.free.sightremote.dialogs.ConfirmationDialog;
import sugar.free.sightremote.utils.CrashlyticsUtil;
import sugar.free.sightremote.utils.HTMLUtil;

public class ChangeActiveBRProfileActivity extends SightActivity implements TaskRunner.ResultCallback, BRProfileAdapter.BRProfileChangeListener, BRProfileAdapter.OnClickListener {

    private ConfirmationDialog confirmationDialog;
    private RecyclerView profileList;
    private BRProfileAdapter adapter;
    private List<BRProfileBlock> brProfileBlocks;
    private List<NameBlock> nameBlocks;
    private int activeProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_change_active_br_profile);

        showManualOverlay();

        profileList = findViewById(R.id.profile_list);
        profileList.setLayoutManager(new LinearLayoutManager(this));
        profileList.setAdapter(adapter = new BRProfileAdapter());
        adapter.setListener(this);
        adapter.setOnClickListener(this);
    }

    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
        statusChanged(getServiceConnector().getStatus());
    }

    @Override
    protected void statusChanged(Status status) {
        if (status == Status.CONNECTED) {
            showLoadingIndicator();
            hideManualOverlay();
            List<Short> blockIDs = new ArrayList<>();
            blockIDs.add(BRProfile1Block.ID);
            blockIDs.add(BRProfile2Block.ID);
            blockIDs.add(BRProfile3Block.ID);
            blockIDs.add(BRProfile4Block.ID);
            blockIDs.add(BRProfile5Block.ID);
            blockIDs.add(BRName1Block.ID);
            blockIDs.add(BRName2Block.ID);
            blockIDs.add(BRName3Block.ID);
            blockIDs.add(BRName4Block.ID);
            blockIDs.add(BRName5Block.ID);
            blockIDs.add(ActiveProfileBlock.ID);
            new ReadConfigurationTaskRunner(getServiceConnector(), blockIDs).fetch(this);
        } else {
            showManualOverlay();
            hideLoadingIndicator();
            if (confirmationDialog != null) confirmationDialog.hide();
        }
    }

    @Override
    public void onResult(Object result) {
        if (result instanceof List) {
            runOnUiThread(() -> {
                List<ConfigurationBlock> blocks = (List<ConfigurationBlock>) result;
                brProfileBlocks = new ArrayList<>();
                brProfileBlocks.add((BRProfileBlock) blocks.get(0));
                brProfileBlocks.add((BRProfileBlock) blocks.get(1));
                brProfileBlocks.add((BRProfileBlock) blocks.get(2));
                brProfileBlocks.add((BRProfileBlock) blocks.get(3));
                brProfileBlocks.add((BRProfileBlock) blocks.get(4));
                nameBlocks = new ArrayList<>();
                nameBlocks.add((NameBlock) blocks.get(5));
                nameBlocks.add((NameBlock) blocks.get(6));
                nameBlocks.add((NameBlock) blocks.get(7));
                nameBlocks.add((NameBlock) blocks.get(8));
                nameBlocks.add((NameBlock) blocks.get(9));
                activeProfile = ((ActiveProfileBlock) blocks.get(10)).getActiveProfile().ordinal();
                adapter.setNameBlocks(nameBlocks);
                adapter.setProfileBlocks(brProfileBlocks);
                adapter.setActiveProfile(activeProfile);
                adapter.notifyDataSetChanged();
                hideLoadingIndicator();
                hideManualOverlay();
            });
        } else if (result == null) {
            Answers.getInstance().logCustom(new CustomEvent("BR Profile Switched"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (confirmationDialog != null) confirmationDialog.hide();
    }

    @Override
    public void onError(Exception e) {
        hideLoadingIndicator();
        runOnUiThread(() -> Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_br_profiles;
    }

    @Override
    public void onProfileChange(int profile) {
        ActiveProfileBlock block = new ActiveProfileBlock();
        block.setActiveProfile(ActiveProfileBlock.ActiveProfile.values()[profile]);
        ArrayList<ConfigurationBlock> blocks = new ArrayList<>();
        blocks.add(block);
        WriteConfigurationTaskRunner taskRunner = new WriteConfigurationTaskRunner(getServiceConnector(), blocks);
        (confirmationDialog = new ConfirmationDialog(this, HTMLUtil.getHTML(R.string.change_br_profile_confirmation), () -> {
            taskRunner.fetch(this);
            adapter.setActiveProfile(profile);
            adapter.notifyDataSetChanged();
        }, () -> adapter.notifyDataSetChanged())).show();
    }

    @Override
    public void onClick(int position) {
        Intent intent = new Intent(this, EditBRProfileActivity.class);
        intent.putExtra("nameBlock", SerializationUtils.serialize(nameBlocks.get(position)));
        intent.putExtra("profileBlock", SerializationUtils.serialize(brProfileBlocks.get(position)));
        startActivity(intent);
    }
}
