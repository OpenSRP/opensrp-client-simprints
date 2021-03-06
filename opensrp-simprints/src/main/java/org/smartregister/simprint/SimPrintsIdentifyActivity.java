package org.smartregister.simprint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;

import java.util.ArrayList;

import static com.simprints.libsimprints.Constants.SIMPRINTS_PACKAGE_NAME;

/**
 * Author : Isaya Mollel on 2019-10-30.
 */
public class SimPrintsIdentifyActivity extends AppCompatActivity {

    public static final String PUT_EXTRA_REQUEST_CODE = "result_code";

    private int REQUEST_CODE;
    private String moduleId;
    private String userId;

    public static void startSimprintsIdentifyActivity(Activity context, String moduleId, int requestCode){
        Intent intent = new Intent(context, SimPrintsIdentifyActivity.class);
        intent.putExtra(Constants.SIMPRINTS_MODULE_ID, moduleId);
        intent.putExtra(PUT_EXTRA_REQUEST_CODE, requestCode);
        context.startActivityForResult(intent, requestCode);
    }
    public static void startSimprintsIdentifyActivity(Activity context, String moduleId, String userId, int requestCode){
        Intent intent = new Intent(context, SimPrintsIdentifyActivity.class);
        intent.putExtra(Constants.SIMPRINTS_MODULE_ID, moduleId);
        intent.putExtra(Constants.SIMPRINTS_USER_ID, userId);
        intent.putExtra(PUT_EXTRA_REQUEST_CODE, requestCode);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!SimPrintsUtils.isPackageInstalled(SIMPRINTS_PACKAGE_NAME,getPackageManager())){
            SimPrintsUtils.downloadSimprintIdApk(this);
            return;
        }
        moduleId = getIntent().getStringExtra(Constants.SIMPRINTS_MODULE_ID);
        userId = getIntent().getStringExtra(Constants.SIMPRINTS_USER_ID);
        REQUEST_CODE = getIntent().getIntExtra(PUT_EXTRA_REQUEST_CODE, 111);

        startIdentification();

    }

    private void startIdentification(){
        try{
            if(TextUtils.isEmpty(userId)){
                userId = SimPrintsLibrary.getInstance().getUserId();
            }
            SimPrintsHelper simPrintsHelper = new SimPrintsHelper(SimPrintsLibrary.getInstance().getProjectId(),
                    userId);
            Intent intent = simPrintsHelper.identify(moduleId);
            startActivityForResult(intent, REQUEST_CODE);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == RESULT_OK && requestCode == REQUEST_CODE){

            Boolean check = data.getBooleanExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, false);
            ArrayList<Identification> identifications = data
                .getParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS);

            ArrayList<SimPrintsIdentification> simPrintsIdentifications = new ArrayList<>();
            String sessionId = data.getStringExtra(Constants.SIMPRINTS_SESSION_ID);

            if (check && identifications != null && identifications.size() > 0){

                for (Identification identification : identifications){
                    SimPrintsIdentification simPrintsIdentification = new SimPrintsIdentification(identification.getGuid());
                    simPrintsIdentification.setTier(identification.getTier());
                    simPrintsIdentification.setConfidence(identification.getConfidence());
                    simPrintsIdentifications.add(simPrintsIdentification);
                }
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra(SimPrintsConstantHelper.INTENT_DATA, simPrintsIdentifications);
//            returnIntent.putExtra(SimPrintsConstantHelper.SIMPRINTS_INTENT_DATA,data);
            returnIntent.putExtra(Constants.SIMPRINTS_SESSION_ID,sessionId);
            setResult(RESULT_OK,returnIntent);
            finish();

        }else {
            showFingerPrintFail(this, new OnDialogButtonClick() {
                @Override
                public void onOkButtonClick() {
                    startIdentification();
                }

                @Override
                public void onCancelButtonClick() {
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED,returnIntent);
                    finish();
                }
            });
        }
    }

    private void showFingerPrintFail(Context context, final OnDialogButtonClick onDialogButtonClick){
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage(getString(R.string.fail_result));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.scan_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogButtonClick.onOkButtonClick();
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogButtonClick.onCancelButtonClick();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

}
