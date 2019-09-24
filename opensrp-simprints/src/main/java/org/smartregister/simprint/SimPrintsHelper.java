package org.smartregister.simprint;

import android.content.Intent;

import com.simprints.libsimprints.SimHelper;

public class SimPrintsHelper {

    private SimHelper simHelper;

    public SimPrintsHelper(String projectId, String userId){
        simHelper = new SimHelper(projectId,userId);
    }
    public Intent enroll(String moduleId){
       return simHelper.register(moduleId);
    }

    public Intent verify(String moduleId, String verifyId){
        return simHelper.verify(moduleId,verifyId);
    }

}