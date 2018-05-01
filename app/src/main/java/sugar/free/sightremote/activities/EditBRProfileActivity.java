package sugar.free.sightremote.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;
import java.util.List;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.ConfigurationBlock;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile1Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile2Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile3Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile4Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfile5Block;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.BRProfileBlock;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.FactoryMinBRAmountBlock;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.MaxBRAmountBlock;
import sugar.free.sightparser.applayer.descriptors.configuration_blocks.NameBlock;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.handling.taskrunners.ReadConfigurationTaskRunner;
import sugar.free.sightparser.handling.taskrunners.WriteConfigurationTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.BRProfileAdapter;
import sugar.free.sightremote.adapters.BRProfileBlockAdapter;
import sugar.free.sightremote.dialogs.ConfirmationDialog;
import sugar.free.sightremote.utils.EditBRBlockDialog;
import sugar.free.sightremote.utils.FixedSizeProfileBlock;
import sugar.free.sightremote.utils.HTMLUtil;

public class EditBRProfileActivity extends SightActivity implements TaskRunner.ResultCallback, BRProfileAdapter.OnClickListener, EditBRBlockDialog.BlockChangedListener {

    private ConfirmationDialog confirmationDialog;

    private NameBlock nameBlock;
    private BRProfileBlock profileBlock;
    private List<FixedSizeProfileBlock> profileBlocks;

    private RecyclerView blockList;
    private BRProfileBlockAdapter adapter;

    private double maxBRAmount = -1;
    private double minBRAmount = -1;
    private int brNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_edit_br_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            nameBlock = (NameBlock) SerializationUtils.deserialize(getIntent().getByteArrayExtra("nameBlock"));
            profileBlock = (BRProfileBlock) SerializationUtils.deserialize(getIntent().getByteArrayExtra("profileBlock"));
        } else {
            nameBlock = (NameBlock) SerializationUtils.deserialize(savedInstanceState.getByteArray("nameBlock"));
            profileBlock = (BRProfileBlock) SerializationUtils.deserialize(savedInstanceState.getByteArray("profileBlock"));
            minBRAmount = savedInstanceState.getDouble("minBRAmount");
            maxBRAmount = savedInstanceState.getDouble("maxBRAmount");
        }

        adjustTitle();

        profileBlocks = FixedSizeProfileBlock.convertToFixed(profileBlock.getProfileBlocks());

        blockList = findViewById(R.id.block_list);
        blockList.setLayoutManager(new LinearLayoutManager(this));
        blockList.setAdapter(adapter = new BRProfileBlockAdapter());
        adapter.setProfileBlocks(profileBlocks);
        adapter.setOnClickListener(this);
        adapter.notifyDataSetChanged();

        if (maxBRAmount == -1) showManualOverlay();
    }

    private void adjustTitle() {
        if (nameBlock.getName().equals("")) {
            brNumber = 0;
            if (profileBlock instanceof BRProfile1Block) brNumber = 1;
            else if (profileBlock instanceof BRProfile2Block) brNumber = 2;
            else if (profileBlock instanceof BRProfile3Block) brNumber = 3;
            else if (profileBlock instanceof BRProfile4Block) brNumber = 4;
            else if (profileBlock instanceof BRProfile5Block) brNumber = 5;
            setTitle(getString(R.string.default_br_name, brNumber));
        }
        else setTitle(nameBlock.getName());
    }

    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
        if (maxBRAmount == -1) {
            statusChanged(getServiceConnector().getStatus());
        }
    }

    @Override
    protected void statusChanged(Status status) {
        if (status == Status.CONNECTED) {
            List<Short> ids = new ArrayList<>();
            ids.add(MaxBRAmountBlock.ID);
            ids.add(FactoryMinBRAmountBlock.ID);
            ReadConfigurationTaskRunner taskRunner = new ReadConfigurationTaskRunner(getServiceConnector(), ids);
            taskRunner.fetch(this);
            hideManualOverlay();
            showLoadingIndicator();
        } else {
            if (confirmationDialog != null) confirmationDialog.hide();
            showManualOverlay();
            hideLoadingIndicator();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray("nameBlock", SerializationUtils.serialize(nameBlock));
        profileBlock.setProfileBlocks(FixedSizeProfileBlock.convertToRelative(profileBlocks));
        outState.putByteArray("profileBlock", SerializationUtils.serialize(profileBlock));
        outState.putDouble("minBRAmount", minBRAmount);
        outState.putDouble("maxBRAmount", maxBRAmount);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (confirmationDialog != null) confirmationDialog.hide();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.edit_br_nav_done) {
            (confirmationDialog = new ConfirmationDialog(this, HTMLUtil.getHTML(R.string.edit_br_profile_confirmation), () -> {
                showManualOverlay();
                List<ConfigurationBlock> blocks = new ArrayList<>();
                blocks.add(nameBlock);
                profileBlock.setProfileBlocks(FixedSizeProfileBlock.convertToRelative(profileBlocks));
                blocks.add(profileBlock);
                WriteConfigurationTaskRunner taskRunner = new WriteConfigurationTaskRunner(getServiceConnector(), blocks);
                showLoadingIndicator();
                taskRunner.fetch(this);
            })).show();
        } else if (item.getItemId() == R.id.edit_br_nav_edit_name) {
            EditText editName = new EditText(this);
            editName.setInputType(InputType.TYPE_CLASS_TEXT);
            editName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(21)});
            editName.setText(nameBlock.getName());
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.edit_name)
                    .setMessage(R.string.leave_empty_for_default_value)
                    .setView(editName)
                    .setPositiveButton(R.string.okay, ((dialog, which) -> {
                        nameBlock.setName(editName.getText().toString());
                        adjustTitle();
                    }))
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean snackbarEnabled() {
        return true;
    }

    @Override
    protected boolean useNavigationDrawer() {
        return false;
    }

    @Override
    public void onResult(Object result) {
        if (result instanceof List) {
            List<ConfigurationBlock> blocks = (List<ConfigurationBlock>) result;
            maxBRAmount = ((MaxBRAmountBlock) blocks.get(0)).getMaximumAmount();
            minBRAmount = ((FactoryMinBRAmountBlock) blocks.get(1)).getMinimumAmount();
            hideLoadingIndicator();
        } else if (result == null) {
            Answers.getInstance().logCustom(new CustomEvent("BR Profile Edited"));
            finish();
        }
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> Toast.makeText(this, getString(R.string.error, e.getClass().getSimpleName()), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onClick(int position) {
        FixedSizeProfileBlock profileBlock = profileBlocks.get(position);
        EditBRBlockDialog.showDialog(this, this, profileBlocks.get(position),
                position == 23,
                position != profileBlocks.size() - 1 ? profileBlock.getEndTime() : profileBlock.getStartTime() + 15,
                minBRAmount, maxBRAmount);
    }

    @Override
    public void onBlockChange(FixedSizeProfileBlock changedBlock, int endTime, double amount) {
        changedBlock.setAmount(amount);
        changedBlock.setEndTime(endTime);
        for (FixedSizeProfileBlock block : new ArrayList<>(profileBlocks)) {
            if (block == changedBlock) continue;
            if (block.getEndTime() <= changedBlock.getStartTime()) continue;
            if (block.getEndTime() <= endTime) profileBlocks.remove(block);
            else if (block.getStartTime() <= endTime) block.setStartTime(endTime);
        }
        FixedSizeProfileBlock latest = profileBlocks.get(profileBlocks.size() - 1);
        if (latest.getEndTime() < 24 * 60)
            profileBlocks.add(new FixedSizeProfileBlock(latest.getEndTime(), 24 * 60, latest.getAmount()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_br_menu, menu);
        return true;
    }
}
