//        Copyright (C) 2016 DigiGene, (www.DigiGene.com)(alinhayati[at]gmail[dot]com)
//
//        Licensed under the Apache License, Version 2.0 (the "License");
//        you may not use this file except in compliance with the License.
//        You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//        Unless required by applicable law or agreed to in writing, software
//        distributed under the License is distributed on an "AS IS" BASIS,
//        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//        See the License for the specific language governing permissions and
//        limitations under the License.

package com.digigene.autoupdate;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.digigene.autoupdate.Dagger.DaggerWrapper;
import com.digigene.autoupdate.model.Response;
import com.digigene.autoupdate.model.ResponseActionFactory;
import com.digigene.autoupdate.model.ServerConnection;
import com.digigene.autoupdate.model.UpdateModel;
import com.digigene.autoupdate.model.UpdateParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import javax.inject.Inject;

public class UpdateRequest {

    @Inject
    UpdateAction updateAction;
    @Inject
    UpdateModel.UpdateFileInfo updateFileInfo;
    @Inject
    ServerConnection serverConnection;
    @Inject
    Context context;
    @Inject
    UpdateModel.DialogTextAttrs dialogTextAttrs;
    private Activity activity;
    private UpdateParams updateParams;

    public UpdateRequest(Activity activity, UpdateParams updateParams) {
        this.activity = activity;
        this.updateParams = updateParams;
        DaggerWrapper.getDependencyComponent(activity, updateParams).inject(this);
        if (updateParams.getUserDialogTextAttrs() != null) {
            dialogTextAttrs.setUserDialogTextAttrs(updateParams.getUserDialogTextAttrs());
        }
    }

    public void update() {
        if (isNetworkAvailable()) {
            new UpdateCommand().execute();
        }
    }

    private class UpdateCommand extends AsyncTask<Void, Void, Void> {
        private Response response;
        private HttpURLConnection httpURLConnection;

        @Override
        protected Void doInBackground(Void... aVoid) {
            httpURLConnection = serverConnection.makeGetRequest();
            response = new ResponseActionFactory(updateParams.getResUnsuccessful())
                    .makeResponse(httpURLConnection, context, activity);
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (ServerConnection.isResponseSuccessful(response.getResponseCode())) {
                updateFileInfo.extractDataFromJson(response, updateParams.getJsonKeys());
                updateDialogTextsAttrs();
                updateAction.update();
            } else {
                return;
            }
        }

        private void updateDialogTextsAttrs() {
            String updateMessageKey = updateParams.getJsonKeys().getUpdateMessageKey();
            if (updateMessageKey != null) {
                JSONObject dataJSON = null;
                try {
                    dataJSON = new JSONObject(response.getResponseString());
                    String updateMessageFromServer = dataJSON.optString(updateMessageKey);
                    if (updateMessageFromServer != null || !updateMessageFromServer.trim()
                            .isEmpty()) {
                        dialogTextAttrs.setForcedUpdateMessageFromServer
                                (updateMessageFromServer);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
